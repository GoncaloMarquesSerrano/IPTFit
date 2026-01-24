package pt.ipt.dam2025.iptfit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade que representa um utilizador na base de dados local
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val username: String,

    val password: String,

    val name: String,

    val email: String,

    val photoPath: String? = null,

    val createdAt: Long = System.currentTimeMillis(),

    val isActive: Boolean = true
)