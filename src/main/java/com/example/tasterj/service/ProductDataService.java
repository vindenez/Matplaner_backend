package com.example.tasterj.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


import java.io.InputStream;

@Service
public class ProductDataService {

    public List<Map<String, Object>> getProducts() {
        return loadJson("products.json");
    }

    public Map<String, List<String>> getBrands() {
        return loadJson("brands.json");
    }

    public Map<String, List<String>> getVendors() {
        return loadJson("vendors.json");
    }

    private <T> T loadJson(String fileName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new RuntimeException("File not found: " + fileName);
            }
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(inputStream, new TypeReference<T>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON file", e);
        }
    }
}

