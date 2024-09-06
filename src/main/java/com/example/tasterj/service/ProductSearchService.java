package com.example.tasterj.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductSearchService {

    @Autowired
    private ResourceLoader resourceLoader;

    private List<Map<String, Object>> products;

    @PostConstruct
    public void init() {
        Resource resource = resourceLoader.getResource("classpath:products.json");
        this.products = loadJson(resource);
    }

    private List<Map<String, Object>> loadJson(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, List<Map<String, Object>>> jsonData = objectMapper.readValue(inputStream, new TypeReference<Map<String, List<Map<String, Object>>>>() {});
            return jsonData.get("data");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load products JSON file", e);
        }
    }

    private List<String> generateSubstrings(String query) {
        String[] words = query.toLowerCase().split("\\s+");
        List<String> substrings = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            for (int j = i; j < words.length; j++) {
                substrings.add(String.join(" ", Arrays.copyOfRange(words, i, j + 1)));
            }
        }
        return substrings;
    }

    public List<Map<String, Object>> searchProducts(String query) {
        List<String> substrings = generateSubstrings(query);

        List<Map<String, Object>> matchedByName = products.stream()
                .filter(product -> ((String) product.get("name")).toLowerCase().contains(substrings.get(0))) // Match name by first substring
                .collect(Collectors.toList());

        return matchedByName.stream()
                .filter(product -> filterByBrandVendorCategoryAndStore(product, substrings))
                .collect(Collectors.toList());
    }

    private boolean filterByBrandVendorCategoryAndStore(Map<String, Object> product, List<String> substrings) {
        final String brand = ((String) product.get("brand")).toLowerCase();
        final String vendor = ((String) product.get("vendor")).toLowerCase();

        List<Map<String, Object>> categories = (List<Map<String, Object>>) product.get("category");

        final String categoryNames;
        if (categories != null) {
            categoryNames = categories.stream()
                    .map(category -> ((String) category.get("name")).toLowerCase())
                    .collect(Collectors.joining(" "));
        } else {
            categoryNames = "";
        }

        Map<String, Object> store = (Map<String, Object>) product.get("store");
        final String storeName = store != null ? ((String) store.get("name")).toLowerCase() : "";

        return substrings.subList(1, substrings.size()).stream()
                .anyMatch(substring -> brand.contains(substring) || vendor.contains(substring) || categoryNames.contains(substring) || storeName.contains(substring));
    }
}
