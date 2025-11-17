package com.example.productapp

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class ProductSyncService(
    private val productService: ProductService,
    private val objectMapper: ObjectMapper
) {
    private val httpClient = HttpClient.newHttpClient()

    fun syncProductsFromApi() {
        try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://famme.no/products.json?limit=250"))
                .GET()
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val json = objectMapper.readTree(response.body())
            val products = json.get("products")
            
            if (products != null && products.isArray) {
                val totalProducts = products.size()
                var savedCount = 0
                var errorCount = 0
                
                for (productNode in products) {
                    try {
                        val title = productNode.get("title")?.asText() ?: continue
                        val vendor = productNode.get("vendor")?.asText()
                        val variants = productNode.get("variants")

                        val firstVariant = if (variants != null && variants.isArray && variants.size() > 0) {
                            variants.get(0)
                        } else null

                        val priceStr = firstVariant?.get("price")?.asText()
                        val price = if (priceStr != null) {
                            try {
                                BigDecimal(priceStr)
                            } catch (e: Exception) {
                                null
                            }
                        } else null

                        val variantsJson = if (variants != null) {
                            objectMapper.writeValueAsString(variants)
                        } else "[]"

                        val product = Product(
                            title = title,
                            price = price,
                            vendor = vendor,
                            variants = variantsJson
                        )

                        productService.save(product)
                        savedCount++
                    } catch (e: Exception) {
                        errorCount++
                        e.printStackTrace()
                    }
                }
                println("Total products in API: $totalProducts, Successfully saved: $savedCount, Errors: $errorCount")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
