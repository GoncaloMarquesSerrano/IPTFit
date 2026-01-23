package pt.ipt.dam2025.iptfit.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de resposta da API Open Food Facts
 * Fonte: https://world.openfoodfacts.org/api/v0/product/{barcode}.json
 */
data class ProductResponse(
    val status: Int,

    @SerializedName("status_verbose")
    val statusVerbose: String?,

    val code: String?,

    val product: Product?
)

data class Product(
    @SerializedName("product_name")
    val productName: String?,

    @SerializedName("brands")
    val brands: String?,

    @SerializedName("quantity")
    val quantity: String?,

    @SerializedName("image_url")
    val imageUrl: String?,

    @SerializedName("image_front_url")
    val imageFrontUrl: String?,

    @SerializedName("nutriments")
    val nutriments: Nutriments?,

    @SerializedName("nutrition_grades")
    val nutritionGrades: String?,

    @SerializedName("categories")
    val categories: String?,

    @SerializedName("ingredients_text")
    val ingredientsText: String?,

    @SerializedName("allergens")
    val allergens: String?
)

data class Nutriments(
    @SerializedName("energy-kcal_100g")
    val energyKcal100g: Float?,

    @SerializedName("proteins_100g")
    val proteins100g: Float?,

    @SerializedName("carbohydrates_100g")
    val carbohydrates100g: Float?,

    @SerializedName("sugars_100g")
    val sugars100g: Float?,

    @SerializedName("fat_100g")
    val fat100g: Float?,

    @SerializedName("saturated-fat_100g")
    val saturatedFat100g: Float?,

    @SerializedName("fiber_100g")
    val fiber100g: Float?,

    @SerializedName("salt_100g")
    val salt100g: Float?
)