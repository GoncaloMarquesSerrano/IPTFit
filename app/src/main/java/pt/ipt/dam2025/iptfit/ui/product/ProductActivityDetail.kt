package pt.ipt.dam2025.iptfit.ui.product

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import pt.ipt.dam2025.iptfit.IPTFitApplication
import pt.ipt.dam2025.iptfit.R
import pt.ipt.dam2025.iptfit.data.local.entity.Consumption
import pt.ipt.dam2025.iptfit.databinding.ActivityProductDetailBinding

/**
 * Activity que mostra detalhes do produto e permite guardar no histórico
 */
class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var app: IPTFitApplication

    private var barcode: String = ""
    private var productName: String = ""
    private var brands: String? = null
    private var imageUrl: String? = null
    private var quantity: String? = null

    // Informação nutricional
    private var energyKcal: Float = 0f
    private var proteins: Float = 0f
    private var carbs: Float = 0f
    private var sugars: Float = 0f
    private var fats: Float = 0f
    private var saturatedFats: Float = 0f
    private var fiber: Float = 0f
    private var salt: Float = 0f
    private var nutriScore: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as IPTFitApplication

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalhes do Produto"

        // Obter dados do Intent
        extractDataFromIntent()

        // Mostrar dados na UI
        displayProductData()

        // Configurar botão de guardar
        binding.btnSaveConsumption.setOnClickListener {
            saveConsumption()
        }

        binding.fabBack.setOnClickListener {
            finish()
        }
    }

    private fun extractDataFromIntent() {
        barcode = intent.getStringExtra("BARCODE") ?: ""
        productName = intent.getStringExtra("PRODUCT_NAME") ?: "Produto Desconhecido"
        brands = intent.getStringExtra("BRANDS")
        imageUrl = intent.getStringExtra("IMAGE_URL")
        quantity = intent.getStringExtra("QUANTITY")

        energyKcal = intent.getFloatExtra("ENERGY_KCAL", 0f)
        proteins = intent.getFloatExtra("PROTEINS", 0f)
        carbs = intent.getFloatExtra("CARBS", 0f)
        sugars = intent.getFloatExtra("SUGARS", 0f)
        fats = intent.getFloatExtra("FATS", 0f)
        saturatedFats = intent.getFloatExtra("SAT_FATS", 0f)
        fiber = intent.getFloatExtra("FIBER", 0f)
        salt = intent.getFloatExtra("SALT", 0f)
        nutriScore = intent.getStringExtra("NUTRI_SCORE")
    }

    private fun displayProductData() {
        // Nome e marca
        binding.tvProductName.text = productName
        binding.tvBrands.text = brands ?: "Marca desconhecida"
        binding.tvBarcode.text = "Código: $barcode"

        // Carregar imagem com Glide
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(binding.ivProductImage)
        }

        // Nutri-Score
        val score = nutriScore
        if (!score.isNullOrEmpty()) {
            binding.tvNutriScore.text = score.uppercase()
            binding.tvNutriScore.setBackgroundColor(getNutriScoreColor(score))
        } else {
            binding.layoutNutriScore.visibility = View.GONE
        }

        // Informação nutricional
        binding.tvEnergy.text = "${formatFloat(energyKcal)} kcal"
        binding.tvProteins.text = "${formatFloat(proteins)} g"
        binding.tvCarbs.text = "${formatFloat(carbs)} g"
        binding.tvFats.text = "${formatFloat(fats)} g"
        binding.tvFiber.text = "${formatFloat(fiber)} g"
        binding.tvSalt.text = "${formatFloat(salt)} g"
    }

    private fun saveConsumption() {
        val userId = app.sessionManager.getUserId()

        if (userId == -1L) {
            Toast.makeText(this, "Erro: Utilizador não autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Validar se tem dados mínimos
        if (barcode.isEmpty() || productName.isEmpty()) {
            Toast.makeText(this, "Dados do produto inválidos", Toast.LENGTH_SHORT).show()
            return
        }

        val notes = binding.etNotes.text.toString().trim()

        val consumption = Consumption(
            userId = userId,
            barcode = barcode,
            productName = productName,
            brands = brands,
            quantity = quantity,
            imageUrl = imageUrl,
            energyKcal = if (energyKcal > 0) energyKcal else null,
            proteins = if (proteins > 0) proteins else null,
            carbohydrates = if (carbs > 0) carbs else null,
            sugars = if (sugars > 0) sugars else null,
            fats = if (fats > 0) fats else null,
            saturatedFats = if (saturatedFats > 0) saturatedFats else null,
            fiber = if (fiber > 0) fiber else null,
            salt = if (salt > 0) salt else null,
            nutriScore = nutriScore,
            notes = if (notes.isNotEmpty()) notes else null
        )

        // Guardar na base de dados
        lifecycleScope.launch {
            try {
                val result = app.repository.addConsumption(consumption)

                result.onSuccess {
                    Toast.makeText(
                        this@ProductDetailActivity,
                        "Produto guardado no histórico!",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }

                result.onFailure { error ->
                    Toast.makeText(
                        this@ProductDetailActivity,
                        "Erro ao guardar: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ProductDetailActivity,
                    "Erro: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Retorna a cor correspondente ao Nutri-Score
     */
    private fun getNutriScoreColor(score: String): Int {
        return when (score.uppercase()) {
            "A" -> Color.parseColor("#038141") // Verde escuro
            "B" -> Color.parseColor("#85BB2F") // Verde claro
            "C" -> Color.parseColor("#FECB02") // Amarelo
            "D" -> Color.parseColor("#EE8100") // Laranja
            "E" -> Color.parseColor("#E63E11") // Vermelho
            else -> Color.GRAY
        }
    }

    /**
     * Formata float para exibição (remove .0 se for inteiro)
     */
    private fun formatFloat(value: Float): String {
        return if (value == 0f) {
            "N/D"
        } else if (value % 1.0f == 0f) {
            value.toInt().toString()
        } else {
            String.format("%.1f", value)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}