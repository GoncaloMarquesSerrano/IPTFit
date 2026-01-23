package pt.ipt.dam2025.iptfit.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pt.ipt.dam2025.iptfit.data.local.entity.Consumption

/**
 * Data Access Object para a entidade Consumption
 */
@Dao
interface ConsumptionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(consumption: Consumption): Long

    @Update
    suspend fun update(consumption: Consumption)

    @Delete
    suspend fun delete(consumption: Consumption)

    @Query("DELETE FROM consumptions WHERE id = :consumptionId")
    suspend fun deleteById(consumptionId: Long)

    @Query("SELECT * FROM consumptions WHERE userId = :userId ORDER BY consumedAt DESC")
    fun getConsumptionsByUser(userId: Long): Flow<List<Consumption>>

    @Query("SELECT * FROM consumptions WHERE userId = :userId AND consumedAt >= :startDate AND consumedAt <= :endDate ORDER BY consumedAt DESC")
    suspend fun getConsumptionsByDateRange(userId: Long, startDate: Long, endDate: Long): List<Consumption>

    @Query("SELECT * FROM consumptions WHERE id = :consumptionId")
    suspend fun getConsumptionById(consumptionId: Long): Consumption?

    @Query("SELECT * FROM consumptions WHERE userId = :userId AND barcode = :barcode ORDER BY consumedAt DESC LIMIT 10")
    suspend fun getConsumptionsByBarcode(userId: Long, barcode: String): List<Consumption>

    @Query("SELECT COUNT(*) FROM consumptions WHERE userId = :userId")
    suspend fun getTotalConsumptionCount(userId: Long): Int

    @Query("SELECT COALESCE(SUM(energyKcal), 0) FROM consumptions WHERE userId = :userId AND consumedAt >= :startDate AND consumedAt <= :endDate")
    suspend fun getTotalCalories(userId: Long, startDate: Long, endDate: Long): Float

    @Query("DELETE FROM consumptions WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)
}