package pt.ipt.dam2025.iptfit.ui.history

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import pt.ipt.dam2025.iptfit.IPTFitApplication
import pt.ipt.dam2025.iptfit.data.local.entity.Consumption
import pt.ipt.dam2025.iptfit.databinding.ActivityHistoryBinding

/**
 * Activity que mostra o histórico de consumos do utilizador
 */
class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var app: IPTFitApplication
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as IPTFitApplication

        // Configurar Toolbar como ActionBar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Habilita botão de voltar
        supportActionBar?.title = "Histórico de Consumos"

        // Configurar clique no botão de voltar da Toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish() // Volta para a MainActivity
        }

        setupRecyclerView()
        loadHistory()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter { consumption ->
            showDeleteConfirmationDialog(consumption)
        }

        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter
    }

    private fun loadHistory() {
        val userId = app.sessionManager.getUserId()

        if (userId == -1L) {
            finish()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Observar mudanças no histórico com Flow
                app.repository.getConsumptionsByUser(userId).collect { consumptions ->
                    binding.progressBar.visibility = View.GONE

                    if (consumptions.isEmpty()) {
                        binding.rvHistory.visibility = View.GONE
                        binding.tvEmptyHistory.visibility = View.VISIBLE
                    } else {
                        binding.rvHistory.visibility = View.VISIBLE
                        binding.tvEmptyHistory.visibility = View.GONE
                        adapter.submitList(consumptions)
                    }
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                e.printStackTrace()
            }
        }
    }

    /**
     * Mostra diálogo de confirmação antes de eliminar
     * (REQUISITO: Validar operações de remoção)
     */
    private fun showDeleteConfirmationDialog(consumption: Consumption) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Consumo")
            .setMessage("Tem a certeza que deseja eliminar este registo?\n\n${consumption.productName}")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteConsumption(consumption)
            }
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteConsumption(consumption: Consumption) {
        lifecycleScope.launch {
            try {
                app.repository.deleteConsumption(consumption)
                // O Flow irá atualizar automaticamente a lista

                android.widget.Toast.makeText(
                    this@HistoryActivity,
                    "Registo eliminado",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    this@HistoryActivity,
                    "Erro ao eliminar: ${e.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Este método também funciona para o botão de voltar
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}