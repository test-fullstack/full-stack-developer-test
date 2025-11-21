package com.example.productapp.api.model

data class ProductApiResponse(
    val products: List<ShopifyProduct>?
)

data class ShopifyProduct(
    val id: Long?,
    val title: String?,
    val vendor: String?,
    val variants: List<ProductVariant>?
)

data class ProductVariant(
    val title: String?,
    val price: String?,
    val sku: String?
)

