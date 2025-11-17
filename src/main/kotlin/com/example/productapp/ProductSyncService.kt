package com.example.productapp

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.scheduling.annotation.Scheduled
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
    private val maxProducts = 50

    @Scheduled(initialDelay = 0, fixedDelay = Long.MAX_VALUE)
    fun syncProducts() {
        try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://famme.no/products.json"))
                .GET()
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val json = objectMapper.readTree(response.body())
            val products = json.get("products")

            if (products != null && products.isArray) {
                var saved = 0
                for (productNode in products) {
                    if (saved >= maxProducts) break

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
                    saved++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

