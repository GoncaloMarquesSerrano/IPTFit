package pt.ipt.dam2025.iptfit.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import pt.ipt.dam2025.iptfit.IPTFitApplication
import pt.ipt.dam2025.iptfit.R
import pt.ipt.dam2025.iptfit.databinding.ActivityMainBinding
import pt.ipt.dam2025.iptfit.ui.about.AboutActivity
import pt.ipt.dam2025.iptfit.ui.auth.LoginActivity
import pt.ipt.dam2025.iptfit.ui.history.HistoryActivity
import pt.ipt.dam2025.iptfit.ui.scanner.ScannerActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var app: IPTFitApplication

    private val PICK_IMAGE_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as IPTFitApplication

        // Verificar autenticação
        if (!app.sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        setupUI()
        loadUserData()
        loadUserProfile()
        loadUriFromPrefs() // Carrega fallback se existir
    }

    private fun setupUI() {
        // Botão para scanner
        binding.btnScanProduct.setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
        }

        // Cards
        val cardHistory = binding.buttonGrid.getChildAt(0) as MaterialCardView
        val cardAbout = binding.buttonGrid.getChildAt(1) as MaterialCardView

        cardHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        cardAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        // Botão logout
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        // Botão para trocar foto de perfil
        binding.btnChangePhoto.setOnClickListener {
            openGallerySimplified()
        }

        // Também permite clicar na foto
        binding.ivUserProfile.setOnClickListener {
            openGallerySimplified()
        }
    }

    private fun openGallerySimplified() {
        // Método que FUNCIONA SEM PERMISSÃO
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        try {
            startActivityForResult(Intent.createChooser(intent, "Escolher foto"), PICK_IMAGE_REQUEST)
        } catch (e: Exception) {
            // Fallback 1
            try {
                val fallbackIntent = Intent(Intent.ACTION_PICK)
                fallbackIntent.type = "image/*"
                startActivityForResult(fallbackIntent, PICK_IMAGE_REQUEST)
            } catch (e2: Exception) {
                // Fallback 2 - método mais básico
                Toast.makeText(this, "Não foi possível abrir a galeria", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val imageUri = data?.data
            imageUri?.let { uri ->
                // Mostrar preview imediato
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .placeholder(R.drawable.logo)
                    .into(binding.ivUserProfile)

                // Salvar no banco de dados
                saveProfileImage(uri)

                // Salvar também no SharedPreferences como backup
                saveUriToPrefs(uri.toString())
            }
        }
    }

    private fun saveProfileImage(imageUri: Uri) {
        lifecycleScope.launch {
            val userId = app.sessionManager.getUserId()
            if (userId != -1L) {
                try {
                    val result = app.repository.saveImageAndUpdateUser(userId, imageUri)

                    if (result.isSuccess) {
                        Toast.makeText(
                            this@MainActivity,
                            "Foto de perfil atualizada!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Se falhar, pelo menos já salvou no SharedPreferences
                        Toast.makeText(
                            this@MainActivity,
                            "Foto salva localmente!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "Foto salva!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun saveUriToPrefs(uriString: String) {
        val prefs = getSharedPreferences("IPTFitPrefs", MODE_PRIVATE)
        prefs.edit().putString("user_photo_uri", uriString).apply()
    }

    private fun loadUriFromPrefs() {
        val prefs = getSharedPreferences("IPTFitPrefs", MODE_PRIVATE)
        val uriString = prefs.getString("user_photo_uri", null)

        uriString?.let {
            try {
                Glide.with(this)
                    .load(Uri.parse(it))
                    .circleCrop()
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(binding.ivUserProfile)
            } catch (e: Exception) {
                // Ignora erro
            }
        }
    }

    private fun loadUserData() {
        val userId = app.sessionManager.getUserId()
        val name = app.sessionManager.getName()

        binding.tvWelcome.text = "Olá, $name!"

        lifecycleScope.launch {
            try {
                val totalConsumptions = app.repository.getTotalConsumptionCount(userId)
                binding.tvTotalScans.text = "$totalConsumptions"

                val startOfDay = getStartOfDay()
                val endOfDay = System.currentTimeMillis()
                val todayCalories = app.repository.getTotalCalories(userId, startOfDay, endOfDay)

                binding.tvTodayCalories.text = "${String.format("%.0f", todayCalories)} kcal"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            val userId = app.sessionManager.getUserId()
            if (userId != -1L) {
                try {
                    val photoPath = app.repository.getUserProfilePhoto(userId)

                    if (!photoPath.isNullOrEmpty()) {
                        try {
                            val imageFile = java.io.File(photoPath)
                            if (imageFile.exists()) {
                                Glide.with(this@MainActivity)
                                    .load(imageFile)
                                    .circleCrop()
                                    .placeholder(R.drawable.logo)
                                    .error(R.drawable.logo)
                                    .into(binding.ivUserProfile)
                            } else {
                                binding.ivUserProfile.setImageResource(R.drawable.logo)
                            }
                        } catch (e: Exception) {
                            binding.ivUserProfile.setImageResource(R.drawable.logo)
                        }
                    }
                } catch (e: Exception) {
                    // Ignora erro
                }
            }
        }
    }

    private fun getStartOfDay(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Terminar Sessão")
            .setMessage("Tem a certeza que deseja terminar a sessão?")
            .setPositiveButton("Sim") { _, _ ->
                app.sessionManager.logout()
                navigateToLogin()
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }
}