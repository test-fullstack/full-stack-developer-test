package com.example.productapp.dto

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

