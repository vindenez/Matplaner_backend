package com.example.tasterj.service;
import com.example.tasterj.model.Ingredient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductDataService {

    @Autowired
    private ResourceLoader resourceLoader;

    public List<Map<String, Object>> getProducts() {
        Map<String, Object> jsonMap = loadJson("classpath:products.json");
        return (List<Map<String, Object>>) jsonMap.get("data");
    }

    public List<Map<String, Object>> getProductsByEANsAndStoreCodes(List<Ingredient> ingredients) {
        List<Map<String, Object>> allProducts = getProducts();

        return allProducts.stream()
                .filter(product -> ingredients.stream().anyMatch(ingredient ->
                        ingredient.getEan().equals(product.get("ean")) &&
                                (ingredient.getStoreCode() == null ||
                                        product.get("store") != null &&
                                                ((Map<String, Object>) product.get("store")).get("code").equals(ingredient.getStoreCode()))))
                .collect(Collectors.toList());
    }

    public Map<String, List<String>> getBrands() {
        return loadJson("classpath:brands.json");
    }

    public Map<String, List<String>> getVendors() {
        return loadJson("classpath:vendors.json");
    }

    private <T> T loadJson(String filePath) {
        Resource resource = resourceLoader.getResource(filePath);
        try (InputStream inputStream = resource.getInputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(inputStream, new TypeReference<T>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to load JSON file from " + filePath, e);
        }
    }
}
