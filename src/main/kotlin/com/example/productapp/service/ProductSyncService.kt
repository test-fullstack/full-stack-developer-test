package com.example.productapp.service

import com.example.productapp.dto.Product
import com.example.productapp.repository.ProductRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class ProductSyncService(
    private val productRepository: ProductRepository,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(ProductSyncService::class.java)
    private val httpClient = HttpClient.newHttpClient()

    @Scheduled(initialDelay = 0, fixedDelay = Long.MAX_VALUE)
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
                val maxProducts = 50 // Limit to 50 products as per requirement
                var savedCount = 0
                var errorCount = 0
                
                for (productNode in products) {
                    // Stop after saving 50 products
                    if (savedCount >= maxProducts) {
                        break
                    }
                    
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

                        productRepository.save(product)
                        savedCount++
                    } catch (e: Exception) {
                        errorCount++
                        logger.error("Error saving product: ${e.message}", e)
                    }
                }
                logger.info("Product sync completed - Total products in API: $totalProducts, Successfully saved: $savedCount (limited to $maxProducts), Errors: $errorCount")
            }
        } catch (e: Exception) {
            logger.error("Error syncing products from API: ${e.message}", e)
        }
    }
}

