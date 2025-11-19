package com.example.productapp.repository

import com.example.productapp.dto.Product
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ProductRepository(
    private val jdbcClient: JdbcClient
) {
    private fun parseSortParams(sortBy: String, order: String, page: Int, pageSize: Int): Triple<String, String, Int> {
        val validSortColumns = setOf("id", "title", "price", "vendor", "created_at")
        val sortColumn = if (validSortColumns.contains(sortBy.lowercase())) sortBy.lowercase() else "id"
        val sortOrder = if (order.lowercase() == "desc") "DESC" else "ASC"
        val offset = page * pageSize
        return Triple(sortColumn, sortOrder, offset)
    }
    
    private fun mapRowToProduct(rs: java.sql.ResultSet): Product {
        return Product(
            id = rs.getLong("id"),
            title = rs.getString("title"),
            price = rs.getObject("price", BigDecimal::class.java),
            vendor = rs.getString("vendor"),
            variants = rs.getString("variants"),
            createdAt = rs.getTimestamp("created_at")?.toLocalDateTime()
        )
    }
    
    fun findAll(sortBy: String = "id", order: String = "asc", page: Int = 0, pageSize: Int = 10): Pair<List<Product>, Int> {
        val (sortColumn, sortOrder, offset) = parseSortParams(sortBy, order, page, pageSize)
        
        val sql = "SELECT id, title, price, vendor, variants, created_at FROM products ORDER BY $sortColumn $sortOrder LIMIT ? OFFSET ?"
        
        val products = jdbcClient.sql(sql)
            .param(pageSize)
            .param(offset)
            .query { rs, _ -> mapRowToProduct(rs) }
            .list()
        
        val totalCount = jdbcClient.sql("SELECT COUNT(*) FROM products")
            .query(Int::class.java)
            .single()
        
        return Pair(products, totalCount)
    }

    fun save(product: Product): Long {
        val id = jdbcClient.sql("INSERT INTO products (title, price, vendor, variants) VALUES (?, ?, ?, ?::jsonb) RETURNING id")
            .param(product.title)
            .param(product.price)
            .param(product.vendor)
            .param(product.variants ?: "[]")
            .query(Long::class.java)
            .single()

        return id
    }

    fun findById(id: Long): Product? {
        return jdbcClient.sql("SELECT id, title, price, vendor, variants, created_at FROM products WHERE id = ?")
            .param(id)
            .query { rs, _ -> mapRowToProduct(rs) }
            .optional()
            .orElse(null)
    }
    
    fun update(product: Product): Boolean {
        val updated = jdbcClient.sql("UPDATE products SET title = ?, price = ?, vendor = ?, variants = ?::jsonb WHERE id = ?")
            .param(product.title)
            .param(product.price)
            .param(product.vendor)
            .param(product.variants ?: "[]")
            .param(product.id!!)
            .update()
        
        return updated > 0
    }
    
    fun delete(id: Long): Boolean {
        val deleted = jdbcClient.sql("DELETE FROM products WHERE id = ?")
            .param(id)
            .update()
        
        return deleted > 0
    }
    
    fun searchByTitle(title: String, sortBy: String = "id", order: String = "asc", page: Int = 0, pageSize: Int = 10): Pair<List<Product>, Int> {
        val (sortColumn, sortOrder, offset) = parseSortParams(sortBy, order, page, pageSize)
        val searchTerm = "%${title.lowercase()}%"
        
        val sql = "SELECT id, title, price, vendor, variants, created_at FROM products WHERE LOWER(title) LIKE ? ORDER BY $sortColumn $sortOrder LIMIT ? OFFSET ?"
        
        val products = jdbcClient.sql(sql)
            .param(searchTerm)
            .param(pageSize)
            .param(offset)
            .query { rs, _ -> mapRowToProduct(rs) }
            .list()
        
        val totalCount = jdbcClient.sql("SELECT COUNT(*) FROM products WHERE LOWER(title) LIKE ?")
            .param(searchTerm)
            .query(Int::class.java)
            .single()
        
        return Pair(products, totalCount)
    }
}

