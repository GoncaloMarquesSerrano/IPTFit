package pt.ipt.dam2025.iptfit.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import pt.ipt.dam2025.iptfit.data.local.entity.User

/**
 * Entidade que representa um consumo de produto pelo utilizador
 */
@Entity(
    tableName = "consumptions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("barcode")]
)
data class Consumption(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: Long, // ID do utilizador que consumiu

    val barcode: String, // Código de barras do produto

    val productName: String, // Nome do produto

    val brands: String?, // Marcas do produto

    val quantity: String?, // Ex: "330ml", "100g"

    val imageUrl: String?,

    // Informação nutricional (por 100g/100ml)
    val energyKcal: Float?,
    val proteins: Float?,
    val carbohydrates: Float?,
    val sugars: Float?,
    val fats: Float?,
    val saturatedFats: Float?,
    val fiber: Float?,
    val salt: Float?,

    val nutriScore: String?, // A, B, C, D, E

    val consumedAt: Long = System.currentTimeMillis(),

    val notes: String? = null // Notas do utilizador
)