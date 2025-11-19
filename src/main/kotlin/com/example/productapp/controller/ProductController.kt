package com.example.productapp.controller

import com.example.productapp.dto.Product
import com.example.productapp.repository.ProductRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ProductController(
    private val productRepository: ProductRepository
) {
    private fun addProductsToModel(
        products: List<Product>,
        totalCount: Int,
        sortBy: String,
        order: String,
        page: Int,
        pageSize: Int,
        model: Model
    ) {
        val totalPages = (totalCount + pageSize - 1) / pageSize
        model.addAttribute("products", products)
        model.addAttribute("sortBy", sortBy)
        model.addAttribute("order", order)
        model.addAttribute("page", page)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalCount", totalCount)
        model.addAttribute("totalPages", totalPages)
    }
    
    @GetMapping("/")
    fun index(): String {
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
        val (products, totalCount) = productRepository.findAll(sortBy, order, page, pageSize)
        addProductsToModel(products, totalCount, sortBy, order, page, pageSize, model)
        return "fragments/product-table"
    }
    
    @PostMapping("/products/load")
    fun loadProducts(
        @RequestParam(required = false, defaultValue = "id") sortBy: String,
        @RequestParam(required = false, defaultValue = "asc") order: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int,
        model: Model
    ): String {
        // Load products from a Postgres database
        val (products, totalCount) = productRepository.findAll(sortBy, order, page, pageSize)
        addProductsToModel(products, totalCount, sortBy, order, page, pageSize, model)
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
        } catch (_: Exception) {
            null
        }

        val product = Product(
            title = title,
            price = productPrice,
            vendor = vendor,
            variants = "[]"
        )

        productRepository.save(product)
        val (products, totalCount) = productRepository.findAll(sortBy, order, page, pageSize)
        addProductsToModel(products, totalCount, sortBy, order, page, pageSize, model)
        return "fragments/product-table"
    }
    
    @GetMapping("/search")
    fun searchPage(): String {
        return "fragments/search"
    }
    
    @GetMapping("/products/search")
    fun searchProducts(
        @RequestParam(required = false, defaultValue = "") query: String,
        @RequestParam(required = false, defaultValue = "id") sortBy: String,
        @RequestParam(required = false, defaultValue = "asc") order: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int,
        model: Model
    ): String {
        val (products, totalCount) = if (query.isBlank()) {
            productRepository.findAll(sortBy, order, page, pageSize)
        } else {
            productRepository.searchByTitle(query, sortBy, order, page, pageSize)
        }
        addProductsToModel(products, totalCount, sortBy, order, page, pageSize, model)
        model.addAttribute("query", query)
        return "fragments/product-table"
    }
    
    @GetMapping("/products/{id}/edit")
    fun editProduct(@PathVariable id: Long, model: Model): String {
        val product = productRepository.findById(id) ?: return "redirect:/"
        model.addAttribute("product", product)
        return "fragments/edit-product"
    }
    
    @PostMapping("/products/{id}/update")
    fun updateProduct(
        @PathVariable id: Long,
        @RequestParam title: String,
        @RequestParam(required = false) price: String?,
        @RequestParam(required = false) vendor: String?
    ): String {
        val productPrice = if (price.isNullOrBlank()) null else try {
            java.math.BigDecimal(price)
        } catch (_: Exception) {
            null
        }
        
        val product = Product(
            id = id,
            title = title,
            price = productPrice,
            vendor = vendor,
            variants = "[]"
        )
        
        productRepository.update(product)
        return "redirect:/"
    }
    
    @PostMapping("/products/{id}/delete")
    fun deleteProduct(
        @PathVariable id: Long,
        @RequestParam(required = false, defaultValue = "id") sortBy: String,
        @RequestParam(required = false, defaultValue = "asc") order: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int,
        model: Model
    ): String {
        productRepository.delete(id)
        val (products, totalCount) = productRepository.findAll(sortBy, order, page, pageSize)
        addProductsToModel(products, totalCount, sortBy, order, page, pageSize, model)
        return "fragments/product-table"
    }
}

