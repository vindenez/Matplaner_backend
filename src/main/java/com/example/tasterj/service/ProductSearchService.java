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

    public Map<String, Object> searchProducts(String query, List<String> selectedStores, int page, int pageSize) {
        Set<String> querySubstrings = new HashSet<>(generateSubstrings(query));

        // Step 1: Filter products that match both the product name and brand/vendor/store/category
        List<Map<String, Object>> filteredProducts = products.stream()
                .filter(product -> filterByBrandVendorCategoryAndStore(product, querySubstrings))
                .collect(Collectors.toList());

        // Step 2: If no products match both conditions, return products that match the query substrings in the name only
        if (filteredProducts.isEmpty()) {
            filteredProducts = products.stream()
                    .filter(product -> productMatchesName(product, new ArrayList<>(querySubstrings)))
                    .collect(Collectors.toList());
        }

        // Step 3: Filter by selected stores if the list is not empty
        if (selectedStores != null && !selectedStores.isEmpty()) {
            filteredProducts = filteredProducts.stream()
                    .filter(product -> {
                        String storeName = (String) ((Map<String, Object>) product.get("store")).get("name");
                        return selectedStores.contains(storeName);
                    })
                    .collect(Collectors.toList());
        }

        int totalProducts = filteredProducts.size();

        // Step 4: Implement pagination
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalProducts);

        List<Map<String, Object>> paginatedProducts = startIndex >= totalProducts
                ? Collections.emptyList()
                : filteredProducts.subList(startIndex, endIndex);

        // Step 5: Create response with paginated products and metadata
        Map<String, Object> response = new HashMap<>();
        response.put("products", paginatedProducts);
        response.put("totalItems", totalProducts);
        response.put("totalPages", (int) Math.ceil((double) totalProducts / pageSize));
        response.put("currentPage", page);

        return response;
    }

    private boolean productMatchesName(Map<String, Object> product, List<String> substrings) {
        String name = Objects.requireNonNullElse((String) product.get("name"), "").toLowerCase();
        return substrings.stream().allMatch(substring -> name.contains(substring));
    }

    private boolean filterByBrandVendorCategoryAndStore(Map<String, Object> product, Set<String> querySubstrings) {
        String name = Objects.requireNonNullElse((String) product.get("name"), "").toLowerCase();
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

        // Step 1: Identify substrings that match brand/vendor/store/category
        Set<String> nonNameSubstrings = querySubstrings.stream()
                .filter(substring -> brand.contains(substring) || vendor.contains(substring) ||
                        store.contains(substring) || categoryNames.stream().anyMatch(cat -> cat.contains(substring)))
                .collect(Collectors.toSet());

        // Step 2: Filter out these substrings from the query to match with product name
        Set<String> remainingSubstrings = querySubstrings.stream()
                .filter(substring -> !nonNameSubstrings.contains(substring))
                .collect(Collectors.toSet());

        // Step 3: Check if the product name matches at least one of the remaining substrings (allow partial match)
        boolean nameMatches = remainingSubstrings.isEmpty() || remainingSubstrings.stream().anyMatch(name::contains);

        // Step 4: Return true if both conditions are satisfied
        return !nonNameSubstrings.isEmpty() && nameMatches;
    }

}
