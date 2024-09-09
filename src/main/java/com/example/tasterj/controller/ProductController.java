package com.example.tasterj.controller;


import com.example.tasterj.service.ProductMatchService;
import com.example.tasterj.service.ProductSearchService;
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
    private ProductMatchService productMatchService;

    @Autowired
    private ProductSearchService productSearchService;

    @PostMapping("/match")
    public Map<String, List<Map<String, Object>>> getMatchedProducts(@RequestBody List<Map<String, String>> ingredients) {
        return productMatchService.findMatches(ingredients);
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

        Map<String, Object> searchResult = productSearchService.searchProducts(query, selectedStores, page, pageSize);

        if (((List<?>) searchResult.get("products")).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No products found for the given query.");
        }

        return ResponseEntity.ok(searchResult);
    }





}

