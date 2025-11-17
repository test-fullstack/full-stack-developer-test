package com.example.productapp

import java.math.BigDecimal
import java.time.LocalDateTime

data class Product(
    val id: Long? = null,
    val title: String,
    val price: BigDecimal?,
    val vendor: String?,
    val variants: String?,
    val createdAt: LocalDateTime? = null
)

data class ProductVariant(
    val title: String?,
    val price: String?,
    val sku: String?
)

data class ShopifyProduct(
    val id: Long?,
    val title: String?,
    val vendor: String?,
    val variants: List<ProductVariant>?
)

