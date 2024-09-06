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

        // Identify which substrings match brand, vendor, store, or category
        Set<String> brandsVendorsStoresCategories = substrings.stream()
                .filter(substring -> products.stream()
                        .anyMatch(product ->
                                productMatchesBrandVendorCategoryOrStore(product, substring)))
                .collect(Collectors.toSet());

        // If no brand/vendor/store/category is found, search by product name only
        if (brandsVendorsStoresCategories.isEmpty()) {
            return products.stream()
                    .filter(product -> productMatchesName(product, substrings))
                    .collect(Collectors.toList());
        }

        // If we have matching brands/vendors/stores/categories, filter products by name and brand/vendor/store/category
        List<Map<String, Object>> filteredProducts = products.stream()
                .filter(product ->
                        productMatchesName(product, substrings) &&
                                productMatchesAnyBrandVendorCategoryOrStore(product, brandsVendorsStoresCategories))
                .collect(Collectors.toList());

        // If no products match both name and brand/vendor/store/category, return products that match brand/vendor/store/category only
        if (filteredProducts.isEmpty()) {
            return products.stream()
                    .filter(product -> productMatchesAnyBrandVendorCategoryOrStore(product, brandsVendorsStoresCategories))
                    .collect(Collectors.toList());
        }

        return filteredProducts;
    }

    private boolean productMatchesName(Map<String, Object> product, List<String> substrings) {
        String name = Objects.requireNonNullElse((String) product.get("name"), "").toLowerCase();
        return substrings.stream().allMatch(substring -> name.contains(substring));
    }

    private boolean productMatchesAnyBrandVendorCategoryOrStore(Map<String, Object> product, Set<String> substrings) {
        String brand = Objects.requireNonNullElse((String) product.get("brand"), "").toLowerCase();
        String vendor = Objects.requireNonNullElse((String) product.get("vendor"), "").toLowerCase();
        List<Map<String, Object>> categories = (List<Map<String, Object>>) product.get("category");
        String store = Objects.requireNonNullElse(
                product.containsKey("store") ? (String) ((Map<String, Object>) product.get("store")).get("name") : null,
                ""
        ).toLowerCase();

        List<String> categoryNames = categories != null
                ? categories.stream()
                .map(cat -> Objects.requireNonNullElse((String) cat.get("name"), "").toLowerCase())
                .collect(Collectors.toList())
                : Collections.emptyList();

        return substrings.stream().anyMatch(substring ->
                brand.contains(substring) ||
                        vendor.contains(substring) ||
                        categoryNames.stream().anyMatch(cat -> cat.contains(substring)) ||
                        store.contains(substring));
    }

    private boolean productMatchesBrandVendorCategoryOrStore(Map<String, Object> product, String substring) {
        String brand = Objects.requireNonNullElse((String) product.get("brand"), "").toLowerCase();
        String vendor = Objects.requireNonNullElse((String) product.get("vendor"), "").toLowerCase();
        List<Map<String, Object>> categories = (List<Map<String, Object>>) product.get("category");
        String store = Objects.requireNonNullElse(
                product.containsKey("store") ? (String) ((Map<String, Object>) product.get("store")).get("name") : null,
                ""
        ).toLowerCase();

        List<String> categoryNames = categories != null
                ? categories.stream()
                .map(cat -> Objects.requireNonNullElse((String) cat.get("name"), "").toLowerCase())
                .collect(Collectors.toList())
                : Collections.emptyList();

        return brand.contains(substring) ||
                vendor.contains(substring) ||
                categoryNames.stream().anyMatch(cat -> cat.contains(substring)) ||
                store.contains(substring);
    }







}
