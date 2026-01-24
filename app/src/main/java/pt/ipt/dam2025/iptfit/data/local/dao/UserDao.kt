package pt.ipt.dam2025.iptfit.data.local.dao

import androidx.room.*
import pt.ipt.dam2025.iptfit.data.local.entity.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): User?

    @Query("SELECT * FROM users WHERE username = :username AND isActive = 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND isActive = 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE username = :username AND password = :password AND isActive = 1")
    suspend fun login(username: String, password: String): User?

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun usernameExists(username: String): Int

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun emailExists(email: String): Int

    @Query("SELECT * FROM users WHERE isActive = 1")
    suspend fun getAllActiveUsers(): List<User>

    // ===== NOVOS MÉTODOS PARA FOTO DE PERFIL =====

    @Query("UPDATE users SET photoPath = :photoPath WHERE id = :userId")
    suspend fun updateUserPhoto(userId: Long, photoPath: String?)

    @Query("SELECT photoPath FROM users WHERE id = :userId")
    suspend fun getUserPhotoPath(userId: Long): String?
}