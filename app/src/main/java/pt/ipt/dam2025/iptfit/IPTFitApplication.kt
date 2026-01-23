package pt.ipt.dam2025.iptfit

import android.app.Application
import pt.ipt.dam2025.iptfit.data.local.AppDatabase
import pt.ipt.dam2025.iptfit.data.remote.RetrofitClient
import pt.ipt.dam2025.iptfit.data.repository.IPTFitRepository
import pt.ipt.dam2025.iptfit.utils.SessionManager

/**
 * Classe Application principal
 * Inicializa componentes globais da aplicação
 */
class IPTFitApplication : Application() {

    // Base de dados
    val database by lazy { AppDatabase.getDatabase(this) }

    // Repositório
    val repository by lazy {
        IPTFitRepository(
            database.userDao(),
            database.consumptionDao(),
            RetrofitClient.api
        )
    }

    // Session Manager
    val sessionManager by lazy { SessionManager(this) }

    override fun onCreate() {
        super.onCreate()
        // Inicializações globais podem ser feitas aqui
    }
}