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
    fun getProducts(
        @RequestParam(required = false, defaultValue = "id") sortBy: String,
        @RequestParam(required = false, defaultValue = "asc") order: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int,
        model: Model
    ): String {
        val (products, totalCount) = productService.findAll(sortBy, order, page, pageSize)
        val totalPages = (totalCount + pageSize - 1) / pageSize
        
        model.addAttribute("products", products)
        model.addAttribute("sortBy", sortBy)
        model.addAttribute("order", order)
        model.addAttribute("page", page)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalCount", totalCount)
        model.addAttribute("totalPages", totalPages)
        return "fragments/product-table"
    }

    @PostMapping("/products")
    fun addProduct(
        @RequestParam title: String,
        @RequestParam(required = false) price: String?,
        @RequestParam(required = false) vendor: String?,
        @RequestParam(required = false, defaultValue = "id") sortBy: String,
        @RequestParam(required = false, defaultValue = "asc") order: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int,
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
        val (products, totalCount) = productService.findAll(sortBy, order, page, pageSize)
        val totalPages = (totalCount + pageSize - 1) / pageSize
        
        model.addAttribute("products", products)
        model.addAttribute("sortBy", sortBy)
        model.addAttribute("order", order)
        model.addAttribute("page", page)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalCount", totalCount)
        model.addAttribute("totalPages", totalPages)
        return "fragments/product-table"
    }
}

