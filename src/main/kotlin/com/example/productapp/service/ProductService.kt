package com.example.productapp.service

import com.example.productapp.dto.Product
import com.example.productapp.repository.ProductRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ProductService(
    private val productRepository: ProductRepository
) {
    
    fun createProduct(
        title: String,
        price: String?,
        vendor: String?
    ): Product {
        val productPrice = if (price.isNullOrBlank()) null else try {
            BigDecimal(price)
        } catch (_: Exception) {
            null
        }

        return Product(
            title = title,
            price = productPrice,
            vendor = vendor,
            variants = "[]"
        )
    }
    
    fun updateProduct(
        id: Long,
        title: String,
        price: String?,
        vendor: String?
    ): Product {
        val productPrice = if (price.isNullOrBlank()) null else try {
            BigDecimal(price)
        } catch (_: Exception) {
            null
        }
        
        return Product(
            id = id,
            title = title,
            price = productPrice,
            vendor = vendor,
            variants = "[]"
        )
    }
}

