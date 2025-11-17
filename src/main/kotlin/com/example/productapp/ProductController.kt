package com.example.productapp

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class ProductController(private val productService: ProductService) {
    @GetMapping("/")
    fun index(model: Model): String {
        return "index"
    }

    @GetMapping("/products")
    fun getProducts(model: Model): String {
        model.addAttribute("products", productService.findAll())
        return "fragments/product-table"
    }

    @PostMapping("/products")
    fun addProduct(
        @RequestParam title: String,
        @RequestParam(required = false) price: String?,
        @RequestParam(required = false) vendor: String?,
        model: Model
    ): String {
        val productPrice = if (price.isNullOrBlank()) null else try {
            java.math.BigDecimal(price)
        } catch (e: Exception) {
            null
        }

        val product = Product(
            title = title,
            price = productPrice,
            vendor = vendor,
            variants = "[]"
        )

        productService.save(product)
        model.addAttribute("products", productService.findAll())
        return "fragments/product-table"
    }
}

