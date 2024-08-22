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
        List<Map<String, Object>> products = productDataService.getProducts();
        Map<String, List<String>> brands = productDataService.getBrands();
        Map<String, List<String>> vendors = productDataService.getVendors();

        List<Map<String, Object>> matchedResults = new ArrayList<>();

        for (Map<String, String> ingredient : ingredients) {
            String ingredientName = ingredient.get("ingredient").toLowerCase();
            String ingredientCategory = ingredient.get("category");

            List<String> relevantBrands = brands.getOrDefault(ingredientCategory, new ArrayList<>());
            List<String> relevantVendors = vendors.getOrDefault(ingredientCategory, new ArrayList<>());

            for (Map<String, Object> product : products) {
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

        return matchedResults;
    }
}

