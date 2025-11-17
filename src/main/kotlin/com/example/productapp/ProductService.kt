package com.example.productapp

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ProductService(
    private val jdbcClient: JdbcClient
) {
    fun findAll(sortBy: String = "id", order: String = "asc", page: Int = 0, pageSize: Int = 10): Pair<List<Product>, Int> {
        val validSortColumns = setOf("id", "title", "price", "vendor", "created_at")
        val sortColumn = if (validSortColumns.contains(sortBy.lowercase())) sortBy.lowercase() else "id"
        val sortOrder = if (order.lowercase() == "desc") "DESC" else "ASC"
        val offset = page * pageSize
        
        val sql = "SELECT id, title, price, vendor, variants, created_at FROM products ORDER BY $sortColumn $sortOrder LIMIT ? OFFSET ?"
        
        val products = jdbcClient.sql(sql)
            .param(pageSize)
            .param(offset)
            .query { rs, _ ->
                Product(
                    id = rs.getLong("id"),
                    title = rs.getString("title"),
                    price = rs.getObject("price", BigDecimal::class.java),
                    vendor = rs.getString("vendor"),
                    variants = rs.getString("variants"),
                    createdAt = rs.getTimestamp("created_at")?.toLocalDateTime()
                )
            }
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

    fun count(): Int {
        return jdbcClient.sql("SELECT COUNT(*) FROM products")
            .query(Int::class.java)
            .single()
    }
}

