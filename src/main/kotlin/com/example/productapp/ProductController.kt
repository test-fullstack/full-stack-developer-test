package com.example.productapp

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ProductController(
    private val productService: ProductService,
    private val productSyncService: ProductSyncService
) {
    @GetMapping("/")
    fun index(model: Model): String {
        val productCount = productService.count()
        model.addAttribute("hasProducts", productCount > 0)
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
    
    @PostMapping("/products/load")
    fun loadProducts(
        @RequestParam(required = false, defaultValue = "id") sortBy: String,
        @RequestParam(required = false, defaultValue = "asc") order: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int,
        model: Model
    ): String {
        productSyncService.syncProductsFromApi()
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
    
    @GetMapping("/search")
    fun searchPage(model: Model): String {
        return "search"
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
            productService.findAll(sortBy, order, page, pageSize)
        } else {
            productService.searchByTitle(query, sortBy, order, page, pageSize)
        }
        val totalPages = (totalCount + pageSize - 1) / pageSize
        
        model.addAttribute("products", products)
        model.addAttribute("query", query)
        model.addAttribute("sortBy", sortBy)
        model.addAttribute("order", order)
        model.addAttribute("page", page)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalCount", totalCount)
        model.addAttribute("totalPages", totalPages)
        return "fragments/product-table"
    }
    
    @GetMapping("/products/{id}/edit")
    fun editProduct(@PathVariable id: Long, model: Model): String {
        val product = productService.findById(id)
        if (product == null) {
            return "redirect:/"
        }
        model.addAttribute("product", product)
        return "edit-product"
    }
    
    @PostMapping("/products/{id}/update")
    fun updateProduct(
        @PathVariable id: Long,
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
            id = id,
            title = title,
            price = productPrice,
            vendor = vendor,
            variants = "[]"
        )
        
        productService.update(product)
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
        productService.delete(id)
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

