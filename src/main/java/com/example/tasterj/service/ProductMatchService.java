package com.example.tasterj.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProductMatchService {

    @Autowired
    private ProductDataService productDataService;

    public List<Map<String, Object>> findMatches(List<Map<String, String>> ingredients) {
        // Validate input
        if (ingredients == null || ingredients.isEmpty()) {
            System.out.println("No ingredients provided for matching.");
            return new ArrayList<>();
        }

        List<Map<String, Object>> products = productDataService.getProducts();
        Map<String, List<String>> brands = productDataService.getBrands();
        Map<String, List<String>> vendors = productDataService.getVendors();

        List<Map<String, Object>> matchedResults = new ArrayList<>();

        for (Map<String, String> ingredient : ingredients) {
            if (ingredient == null || ingredient.get("ingredient") == null || ingredient.get("category") == null) {
                System.out.println("Invalid ingredient entry: " + ingredient);
                continue; // Skip invalid ingredient entries
            }

            String ingredientName = ingredient.get("ingredient").toLowerCase();
            String ingredientCategory = ingredient.get("category");

            List<String> relevantBrands = brands.getOrDefault(ingredientCategory, new ArrayList<>());
            List<String> relevantVendors = vendors.getOrDefault(ingredientCategory, new ArrayList<>());

            for (Map<String, Object> product : products) {
                if (product == null || product.get("name") == null || product.get("brand") == null || product.get("vendor") == null) {
                    System.out.println("Invalid product entry: " + product);
                    continue; // Skip invalid product entries
                }

                String productName = ((String) product.get("name")).toLowerCase();
                String productBrand = (String) product.get("brand");
                String productVendor = (String) product.get("vendor");

                if (productName.contains(ingredientName)) {
                    if (relevantBrands.contains(productBrand) || relevantVendors.contains(productVendor)) {
                        matchedResults.add(product);
                    }
                }
            }
        }

        System.out.println("Matched results: " + matchedResults.size());
        return matchedResults;
    }
}
