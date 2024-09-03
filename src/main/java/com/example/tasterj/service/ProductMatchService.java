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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductMatchService {

    @Autowired
    private ResourceLoader resourceLoader;

    private List<Map<String, Object>> matchedProducts;

    @PostConstruct
    public void init() {
        Resource resource = resourceLoader.getResource("classpath:matched_products.json");
        this.matchedProducts = loadJson(resource);
    }

    private List<Map<String, Object>> loadJson(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to load matched products JSON file", e);
        }
    }

    // Updated method to find matches for a list of ingredients
    public List<Map<String, Object>> findMatches(List<Map<String, String>> ingredients) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, String> ingredient : ingredients) {
            String ingredientName = ingredient.get("ingredient");

            Optional<Map<String, Object>> match = matchedProducts.stream()
                    .filter(m -> ingredientName.equalsIgnoreCase((String) m.get("ingredient")))
                    .findFirst();

            match.ifPresent(m -> result.addAll((List<Map<String, Object>>) m.getOrDefault("matches", Collections.emptyList())));
        }

        return result;
    }
}
