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

    @Scheduled(initialDelay = 0, fixedDelay = Long.MAX_VALUE)
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
                                    java.math.BigDecimal(priceStr)
                                } catch (_: Exception) {
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
                    logger.info("Product sync completed - Total products in API: $totalProducts, Successfully saved: $savedCount, Errors: $errorCount")
                }
            }
        } catch (e: Exception) {
            logger.error("Error syncing products from API: ${e.message}", e)
        }
    }
}

