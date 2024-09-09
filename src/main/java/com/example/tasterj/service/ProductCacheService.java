package com.example.tasterj.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductCacheService {

    private final RestTemplate restTemplate;

    public ProductCacheService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Cacheable(value = "productCache", key = "7311041027134")
    public Product getProductByEan(String ean) {
        String apiUrl = "https://kassal.app/api/v1/products?ean=" + ean;
        ResponseEntity<Product> response = restTemplate.getForEntity(apiUrl, Product.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to fetch product data from API");
        }
    }
}
