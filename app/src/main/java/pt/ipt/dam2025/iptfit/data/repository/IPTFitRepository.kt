package pt.ipt.dam2025.iptfit.data.repository

import kotlinx.coroutines.flow.Flow
import pt.ipt.dam2025.iptfit.data.local.dao.ConsumptionDao
import pt.ipt.dam2025.iptfit.data.local.dao.UserDao
import pt.ipt.dam2025.iptfit.data.local.entity.Consumption
import pt.ipt.dam2025.iptfit.data.local.entity.User
import pt.ipt.dam2025.iptfit.data.remote.OpenFoodFactsApi
import pt.ipt.dam2025.iptfit.data.remote.model.ProductResponse
import retrofit2.Response

/**
 * Repositório que centraliza o acesso aos dados (local e remoto)
 */
class IPTFitRepository(
    private val userDao: UserDao,
    private val consumptionDao: ConsumptionDao,
    private val api: OpenFoodFactsApi
) {

    // ==================== USER OPERATIONS ====================

    suspend fun registerUser(user: User): Result<Long> {
        return try {
            // Validar se username já existe
            if (userDao.usernameExists(user.username) > 0) {
                return Result.failure(Exception("Nome de utilizador já existe"))
            }

            // Validar se email já existe
            if (userDao.emailExists(user.email) > 0) {
                return Result.failure(Exception("Email já está registado"))
            }

            val userId = userDao.insert(user)
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(username: String, password: String): Result<User> {
        return try {
            val user = userDao.login(username, password)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Credenciais inválidas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId)
    }

    suspend fun updateUser(user: User) {
        userDao.update(user)
    }

    // ==================== CONSUMPTION OPERATIONS ====================

    suspend fun addConsumption(consumption: Consumption): Result<Long> {
        return try {
            val id = consumptionDao.insert(consumption)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getConsumptionsByUser(userId: Long): Flow<List<Consumption>> {
        return consumptionDao.getConsumptionsByUser(userId)
    }

    suspend fun getConsumptionById(consumptionId: Long): Consumption? {
        return consumptionDao.getConsumptionById(consumptionId)
    }

    suspend fun updateConsumption(consumption: Consumption) {
        consumptionDao.update(consumption)
    }

    suspend fun deleteConsumption(consumption: Consumption) {
        consumptionDao.delete(consumption)
    }

    suspend fun getTotalConsumptionCount(userId: Long): Int {
        return consumptionDao.getTotalConsumptionCount(userId)
    }

    suspend fun getConsumptionsByDateRange(userId: Long, startDate: Long, endDate: Long): List<Consumption> {
        return consumptionDao.getConsumptionsByDateRange(userId, startDate, endDate)
    }

    suspend fun getTotalCalories(userId: Long, startDate: Long, endDate: Long): Float {
        return consumptionDao.getTotalCalories(userId, startDate, endDate)
    }

    // ==================== API OPERATIONS ====================

    suspend fun getProductByBarcode(barcode: String): Result<ProductResponse> {
        return try {
            val response = api.getProduct(barcode)
            if (response.isSuccessful && response.body() != null) {
                val productResponse = response.body()!!
                if (productResponse.status == 1) {
                    Result.success(productResponse)
                } else {
                    Result.failure(Exception("Produto não encontrado na base de dados"))
                }
            } else {
                Result.failure(Exception("Erro ao consultar API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erro de rede: ${e.message}"))
        }
    }
}