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
                .filter(product -> ((String) product.get("name")).toLowerCase().contains(substrings.get(0)))
                .collect(Collectors.toList());

        return matchedByName.stream()
                .peek(product -> {
                    String defaultImage = "https://media.istockphoto.com/id/1472933890/vector/no-image-vector-symbol-missing-available-icon-no-gallery-for-this-moment-placeholder.jpg?s=612x612&w=0&k=20&c=Rdn-lecwAj8ciQEccm0Ep2RX50FCuUJOaEM8qQjiLL0=";
                    product.put("image", Objects.requireNonNullElse((String) product.get("image"), defaultImage));
                })
                .filter(product -> filterByBrandVendorCategoryAndStore(product, new HashSet<>(substrings)))
                .collect(Collectors.toList());
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

        return querySubstrings.stream().anyMatch(substring ->
                name.contains(substring) ||
                        brand.contains(substring) ||
                        vendor.contains(substring) ||
                        categoryNames.stream().anyMatch(cat -> cat.contains(substring)) ||
                        store.contains(substring)
        );
    }

}
