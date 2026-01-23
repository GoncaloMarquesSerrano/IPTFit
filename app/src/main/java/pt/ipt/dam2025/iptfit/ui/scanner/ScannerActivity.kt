package pt.ipt.dam2025.iptfit.ui.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import pt.ipt.dam2025.iptfit.IPTFitApplication
import pt.ipt.dam2025.iptfit.databinding.ActivityScannerBinding
import pt.ipt.dam2025.iptfit.ui.product.ProductDetailActivity
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Activity para scanner de códigos de barras usando CameraX e ML Kit
 */
class ScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private lateinit var cameraExecutor: ExecutorService
    private var isProcessing = false // Flag para evitar múltiplos scans

    companion object {
        private const val TAG = "ScannerActivity"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Verificar permissões
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        // Botão para introduzir código manualmente
        binding.btnManualInput.setOnClickListener {
            showManualInputDialog()
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            // Scanner ML Kit
            val scanner = BarcodeScanning.getClient()

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                val mediaImage = imageProxy.image

                if (mediaImage != null && !isProcessing) {
                    val image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )

                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                barcode.rawValue?.let { barcodeValue ->
                                    if (!isProcessing) {
                                        isProcessing = true
                                        Log.d(TAG, "Código de barras lido: $barcodeValue")
                                        onBarcodeDetected(barcodeValue)
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Erro ao ler código de barras", e)
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }

            // Câmara traseira
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e(TAG, "Falha ao iniciar câmara", e)
                Toast.makeText(this, "Erro ao iniciar câmara", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun onBarcodeDetected(barcode: String) {
        // Validar código de barras
        if (!isValidBarcode(barcode)) {
            Toast.makeText(this, "Código de barras inválido", Toast.LENGTH_SHORT).show()
            isProcessing = false
            return
        }

        // Consultar API
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE
                binding.tvStatus.text = "A procurar produto..."

                val app = application as IPTFitApplication
                val result = app.repository.getProductByBarcode(barcode)

                result.onSuccess { productResponse ->
                    // Navegar para detalhes do produto
                    val intent = Intent(this@ScannerActivity, ProductDetailActivity::class.java)
                    intent.putExtra("BARCODE", barcode)
                    intent.putExtra("PRODUCT_NAME", productResponse.product?.productName)
                    intent.putExtra("BRANDS", productResponse.product?.brands)
                    intent.putExtra("IMAGE_URL", productResponse.product?.imageFrontUrl ?: productResponse.product?.imageUrl)

                    // Informação nutricional
                    productResponse.product?.nutriments?.let { nutri ->
                        intent.putExtra("ENERGY_KCAL", nutri.energyKcal100g ?: 0f)
                        intent.putExtra("PROTEINS", nutri.proteins100g ?: 0f)
                        intent.putExtra("CARBS", nutri.carbohydrates100g ?: 0f)
                        intent.putExtra("SUGARS", nutri.sugars100g ?: 0f)
                        intent.putExtra("FATS", nutri.fat100g ?: 0f)
                        intent.putExtra("SAT_FATS", nutri.saturatedFat100g ?: 0f)
                        intent.putExtra("FIBER", nutri.fiber100g ?: 0f)
                        intent.putExtra("SALT", nutri.salt100g ?: 0f)
                    }

                    intent.putExtra("NUTRI_SCORE", productResponse.product?.nutritionGrades)
                    intent.putExtra("QUANTITY", productResponse.product?.quantity)

                    startActivity(intent)
                    finish()
                }

                result.onFailure { error ->
                    Toast.makeText(
                        this@ScannerActivity,
                        "Produto não encontrado: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    isProcessing = false
                    binding.tvStatus.text = "Aponte a câmara para um código de barras"
                }

            } catch (e: Exception) {
                Log.e(TAG, "Erro ao consultar API", e)
                Toast.makeText(this@ScannerActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                isProcessing = false
                binding.tvStatus.text = "Aponte a câmara para um código de barras"
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun isValidBarcode(barcode: String): Boolean {
        // Validar EAN-13 (13 dígitos), EAN-8 (8 dígitos), UPC-A (12 dígitos)
        return barcode.matches(Regex("^\\d{8}$|^\\d{12,13}$"))
    }

    private fun showManualInputDialog() {
        val editText = android.widget.EditText(this)
        editText.hint = "Código de barras"
        editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Introduzir Código Manualmente")
            .setView(editText)
            .setPositiveButton("Procurar") { _, _ ->
                val barcode = editText.text.toString().trim()
                if (barcode.isNotEmpty()) {
                    if (isValidBarcode(barcode)) {
                        onBarcodeDetected(barcode)
                    } else {
                        Toast.makeText(this, "Código inválido. Use 8, 12 ou 13 dígitos.", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var permissionGranted = true
        permissions.entries.forEach {
            if (it.key in REQUIRED_PERMISSIONS && !it.value) {
                permissionGranted = false
            }
        }

        if (!permissionGranted) {
            Toast.makeText(this, "Permissão da câmara negada", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            startCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}