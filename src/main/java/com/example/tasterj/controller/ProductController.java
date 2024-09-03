package com.example.tasterj.controller;


import com.example.tasterj.service.ProductMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductMatchService productMatchService;

    @PostMapping("/match")
    public Map<String, List<Map<String, Object>>> getMatchedProducts(@RequestBody List<Map<String, String>> ingredients) {
        return productMatchService.findMatches(ingredients);
    }

}

