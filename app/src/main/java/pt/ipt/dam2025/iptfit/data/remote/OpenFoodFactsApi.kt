package pt.ipt.dam2025.iptfit.data.remote

import pt.ipt.dam2025.iptfit.data.remote.model.ProductResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interface da API Open Food Facts
 * Base URL: https://world.openfoodfacts.org
 * Documentação: https://openfoodfacts.github.io/openfoodfacts-server/api/
 */
interface OpenFoodFactsApi {

    /**
     * Obtém informação de um produto pelo código de barras
     * @param barcode Código de barras EAN-13, EAN-8, UPC-A ou UPC-E
     * @return Resposta com dados do produto
     */
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProduct(
        @Path("barcode") barcode: String
    ): Response<ProductResponse>
}