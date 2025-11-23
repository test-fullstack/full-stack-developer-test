package com.example.productapp.api.service

import com.example.productapp.dto.Product
import com.example.productapp.repository.ProductRepository
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.math.BigDecimal

@Service
class ProductSyncService(
    private val productRepository: ProductRepository,
    private val objectMapper: ObjectMapper,
    private val fammeRestClient: RestClient
) {
    private val logger = LoggerFactory.getLogger(ProductSyncService::class.java)

    @Scheduled(initialDelay = 0, fixedDelay = 86400000) // Run daily (24 hours = 86400000 ms)
    fun syncProductsFromApi() {
        try {
            val responseBody = fammeRestClient.get()
                .uri("/products.json")
                .retrieve()
                .body(String::class.java)

            if (responseBody != null) {
                val json = objectMapper.readTree(responseBody)
                val products = json.get("products")
                
                if (products != null && products.isArray) {
                    val totalProducts = products.size()
                    var savedCount = 0
                    var errorCount = 0
                    val maxProducts = 50
                    
                    for ((index, productNode) in products.withIndex()) {
                        // Limit to 50 products
                        if (index >= maxProducts) {
                            logger.info("Reached product limit of $maxProducts, stopping sync")
                            break
                        }
                        
                        try {
                            val title = productNode.get("title")?.asText() ?: continue
                            val vendor = productNode.get("vendor")?.asText()
                            val variants = productNode.get("variants")

                            // Extract all variant prices and calculate minimum price
                            val minPrice = extractMinPriceFromVariants(variants)

                            // Store all variants as JSONB
                            val variantsJson = if (variants != null) {
                                objectMapper.writeValueAsString(variants)
                            } else "[]"

                            val product = Product(
                                title = title,
                                price = minPrice,
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
                    logger.info("Product sync completed - Total products in API: $totalProducts, Processed: ${minOf(maxProducts, totalProducts)}, Successfully saved: $savedCount, Errors: $errorCount")
                }
            }
        } catch (e: Exception) {
            logger.error("Error syncing products from API: ${e.message}", e)
        }
    }
    
    /**
     * Extracts the minimum price from all product variants.
     * If no variants or no valid prices found, returns null.
     */
    private fun extractMinPriceFromVariants(variants: JsonNode?): BigDecimal? {
        if (variants == null || !variants.isArray || variants.size() == 0) {
            return null
        }
        
        val prices = mutableListOf<BigDecimal>()
        
        for (variant in variants) {
            val priceStr = variant.get("price")?.asText()
            if (priceStr != null) {
                try {
                    val price = java.math.BigDecimal(priceStr)
                    prices.add(price)
                } catch (_: Exception) {
                    // Skip invalid price
                }
            }
        }
        
        return if (prices.isNotEmpty()) {
            prices.minOrNull()
        } else {
            null
        }
    }
}

