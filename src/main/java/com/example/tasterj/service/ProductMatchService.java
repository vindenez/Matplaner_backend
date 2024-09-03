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

    public Map<String, List<Map<String, Object>>> findMatches(List<Map<String, String>> ingredients) {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();

        for (Map<String, String> ingredient : ingredients) {
            String ingredientName = ingredient.get("ingredient");

            Optional<Map<String, Object>> match = matchedProducts.stream()
                    .filter(m -> ingredientName.equalsIgnoreCase((String) m.get("ingredient")))
                    .findFirst();

            List<Map<String, Object>> matches = match
                    .map(m -> (List<Map<String, Object>>) m.getOrDefault("matches", Collections.emptyList()))
                    .orElse(Collections.emptyList());

            result.put(ingredientName, matches);
        }

        return result;
    }

}
