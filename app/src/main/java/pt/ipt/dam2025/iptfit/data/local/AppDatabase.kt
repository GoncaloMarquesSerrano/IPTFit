package pt.ipt.dam2025.iptfit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.ipt.dam2025.iptfit.data.local.dao.ConsumptionDao
import pt.ipt.dam2025.iptfit.data.local.dao.UserDao
import pt.ipt.dam2025.iptfit.data.local.entity.Consumption
import pt.ipt.dam2025.iptfit.data.local.entity.User

/**
 * Base de dados local da aplicação usando Room
 */
@Database(
    entities = [User::class, Consumption::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun consumptionDao(): ConsumptionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "iptfit_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Popula a base de dados com utilizadores de teste
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                populateDatabase(context)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Cria utilizadores de teste na primeira execução
         */
        private suspend fun populateDatabase(context: Context) {
            val database = getDatabase(context)
            val userDao = database.userDao()

            // Hash SHA-256 (mesmo algoritmo usado no login)
            val hashPassword = { password: String ->
                java.security.MessageDigest.getInstance("SHA-256")
                    .digest(password.toByteArray())
                    .joinToString("") { "%02x".format(it) }
            }

            val user1 = User(
                username = "gs42",
                password = hashPassword("123qwe"),
                name = "Goncalo Marques Serrano",
                email = "gs@iptfit.pt"
            )

            val user2 = User(
                username = "diogoj",
                password = hashPassword("qwe123"),
                name = "Diogo Vicente Jorge",
                email = "dj@iptfit.pt"
            )


            try {
                userDao.insert(user1)
                userDao.insert(user2)
                android.util.Log.d("AppDatabase", "Utilizadores de teste criados com sucesso!")
            } catch (e: Exception) {
                android.util.Log.e("AppDatabase", "Erro ao criar utilizadores: ${e.message}")
            }
        }
    }
}