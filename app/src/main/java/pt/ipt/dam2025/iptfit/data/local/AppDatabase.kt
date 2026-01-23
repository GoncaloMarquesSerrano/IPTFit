package pt.ipt.dam2025.iptfit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}