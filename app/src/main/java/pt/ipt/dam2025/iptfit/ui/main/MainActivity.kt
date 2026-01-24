package pt.ipt.dam2025.iptfit.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import pt.ipt.dam2025.iptfit.IPTFitApplication
import pt.ipt.dam2025.iptfit.R
import pt.ipt.dam2025.iptfit.databinding.ActivityMainBinding
import pt.ipt.dam2025.iptfit.ui.about.AboutActivity
import pt.ipt.dam2025.iptfit.ui.auth.LoginActivity
import pt.ipt.dam2025.iptfit.ui.history.HistoryActivity
import pt.ipt.dam2025.iptfit.ui.scanner.ScannerActivity

/**
 * Activity principal da aplicação
 * Ecrã inicial após login
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var app: IPTFitApplication

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
    }

    private fun setupUI() {
        // Botão para scanner
        binding.btnScanProduct.setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
        }

        // ENCONTRAR OS CARDS DENTRO DO buttonGrid
        // O primeiro filho (índice 0) é o card do Histórico
        // O segundo filho (índice 1) é o card do Sobre
        val cardHistory = binding.buttonGrid.getChildAt(0) as MaterialCardView
        val cardAbout = binding.buttonGrid.getChildAt(1) as MaterialCardView

        // Card para histórico - clique em TODO O CARD
        cardHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // Card para sobre - clique em TODO O CARD
        cardAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        // Botão logout
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun loadUserData() {
        val userId = app.sessionManager.getUserId()
        val name = app.sessionManager.getName()

        binding.tvWelcome.text = "Olá, $name!"

        // Carregar estatísticas
        lifecycleScope.launch {
            try {
                val totalConsumptions = app.repository.getTotalConsumptionCount(userId)
                binding.tvTotalScans.text = "$totalConsumptions"

                // Calcular calorias de hoje
                val startOfDay = getStartOfDay()
                val endOfDay = System.currentTimeMillis()
                val todayCalories = app.repository.getTotalCalories(userId, startOfDay, endOfDay)

                binding.tvTodayCalories.text = "${String.format("%.0f", todayCalories)} kcal"
            } catch (e: Exception) {
                e.printStackTrace()
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
        loadUserData() // Atualizar dados ao voltar
    }
}