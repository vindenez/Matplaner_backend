package com.example.tasterj.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
public class ProductDataService {

    private List<Map<String, Object>> products;
    private Map<String, List<String>> brands;
    private Map<String, List<String>> vendors;

    @PostConstruct
    public void loadProductData() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Load kassalapp_data.json
            String productsJson = new String(Files.readAllBytes(Paths.get("kassalapp_data.json")));
            Map<String, List<Map<String, Object>>> productsDataMap = objectMapper.readValue(productsJson, new TypeReference<Map<String, List<Map<String, Object>>>>() {});
            this.products = productsDataMap.get("data");

            // Load brands.json
            String brandsJson = new String(Files.readAllBytes(Paths.get("brands.json")));
            Map<String, Map<String, List<String>>> brandsDataMap = objectMapper.readValue(brandsJson, new TypeReference<Map<String, Map<String, List<String>>>>() {});
            this.brands = brandsDataMap.get("Brands");

            // Load vendors.json
            String vendorsJson = new String(Files.readAllBytes(Paths.get("vendors.json")));
            Map<String, Map<String, List<String>>> vendorsDataMap = objectMapper.readValue(vendorsJson, new TypeReference<Map<String, Map<String, List<String>>>>() {});
            this.vendors = vendorsDataMap.get("Vendors");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getProducts() {
        return products;
    }

    public Map<String, List<String>> getBrands() {
        return brands;
    }

    public Map<String, List<String>> getVendors() {
        return vendors;
    }
}

