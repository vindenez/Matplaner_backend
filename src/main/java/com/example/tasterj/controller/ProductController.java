package com.example.tasterj.controller;


import com.example.tasterj.model.Product;
import com.example.tasterj.service.ProductDataService;
import com.example.tasterj.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final int QUERY_CHAR_LIMIT = 50;

    @Autowired
    private ProductService productService;


    @PostMapping("/match")
    public Map<String, List<Map<String, Object>>> getMatchedProducts(@RequestBody List<Map<String, String>> ingredients) {
        return productService.findMatches(ingredients);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) List<String> selectedStores) {

        if (query.length() > QUERY_CHAR_LIMIT) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Search query exceeds character limit of " + QUERY_CHAR_LIMIT + " characters.");
        }

        Map<String, Object> searchResult = productService.searchProducts(query, selectedStores, page, pageSize);

        if (((List<?>) searchResult.get("products")).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No products found for the given query.");
        }

        return ResponseEntity.ok(searchResult);
    }

}

