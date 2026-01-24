package pt.ipt.dam2025.iptfit.data.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import pt.ipt.dam2025.iptfit.data.local.dao.ConsumptionDao
import pt.ipt.dam2025.iptfit.data.local.dao.UserDao
import pt.ipt.dam2025.iptfit.data.local.entity.Consumption
import pt.ipt.dam2025.iptfit.data.local.entity.User
import pt.ipt.dam2025.iptfit.data.remote.OpenFoodFactsApi
import pt.ipt.dam2025.iptfit.data.remote.model.ProductResponse
import java.io.File
import java.io.FileOutputStream

/**
 * Repositório que centraliza o acesso aos dados (local e remoto)
 */
class IPTFitRepository(
    private val userDao: UserDao,
    private val consumptionDao: ConsumptionDao,
    private val api: OpenFoodFactsApi,
    private val context: Context  // PARÂMETRO ADICIONADO
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

    // ==================== FOTO DE PERFIL ====================

    suspend fun updateUserProfilePhoto(userId: Long, photoPath: String?) {
        userDao.updateUserPhoto(userId, photoPath)
    }

    suspend fun getUserProfilePhoto(userId: Long): String? {
        return userDao.getUserPhotoPath(userId)
    }

    suspend fun saveImageAndUpdateUser(userId: Long, imageUri: android.net.Uri): Result<String> {
        return try {
            val savedPath = saveImageToInternalStorage(imageUri, userId)
            updateUserProfilePhoto(userId, savedPath)
            Result.success(savedPath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveImageToInternalStorage(uri: android.net.Uri, userId: Long): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Não foi possível abrir a imagem")

        val profileDir = File(context.filesDir, "profiles")
        if (!profileDir.exists()) {
            profileDir.mkdirs()
        }

        val fileName = "user_${userId}_profile.jpg"
        val file = File(profileDir, fileName)

        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()

        return file.absolutePath
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