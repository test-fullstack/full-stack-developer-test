package com.example.productapp.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class WebClientConfig {
    
    @Bean
    fun fammeRestClient(): RestClient {
        return RestClient.builder()
            .baseUrl("https://famme.no")
            .build()
    }
}

