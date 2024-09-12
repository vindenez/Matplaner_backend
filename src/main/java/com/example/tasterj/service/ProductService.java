package com.example.tasterj.service;

import com.example.tasterj.model.Product;
import com.example.tasterj.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Map<String, Object> searchProducts(String query, List<String> selectedStores, int page, int pageSize) {
        Set<String> querySubstrings = new HashSet<>(generateSubstrings(query));

        // Fetch all products from MongoDB
        List<Product> allProducts = productRepository.findAll();

        // Step 1: Filter products that match both the product name and brand/vendor/store/category
        List<Product> filteredProducts = allProducts.stream()
                .filter(product -> filterByBrandVendorCategoryAndStore(product, querySubstrings))
                .collect(Collectors.toList());

        // Step 2: If no products match both conditions, return products that match the query substrings in the name only
        if (filteredProducts.isEmpty()) {
            filteredProducts = allProducts.stream()
                    .filter(product -> productMatchesName(product, new ArrayList<>(querySubstrings)))
                    .collect(Collectors.toList());
        }

        // Step 3: Filter by selected stores if the list is not empty
        if (selectedStores != null && !selectedStores.isEmpty()) {
            filteredProducts = filteredProducts.stream()
                    .filter(product -> selectedStores.contains(product.getStore().getName()))
                    .collect(Collectors.toList());
        }

        int totalProducts = filteredProducts.size();

        // Step 4: Implement pagination
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalProducts);

        List<Product> paginatedProducts = startIndex >= totalProducts
                ? Collections.emptyList()
                : filteredProducts.subList(startIndex, endIndex);

        // Step 5: Convert Product objects to Map<String, Object>
        List<Map<String, Object>> productMaps = paginatedProducts.stream()
                .map(this::convertProductToMap)
                .collect(Collectors.toList());

        // Step 6: Create response with paginated products and metadata
        Map<String, Object> response = new HashMap<>();
        response.put("products", productMaps);
        response.put("totalItems", totalProducts);
        response.put("totalPages", (int) Math.ceil((double) totalProducts / pageSize));
        response.put("currentPage", page);

        return response;
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

    private boolean productMatchesName(Product product, List<String> substrings) {
        String name = product.getName() != null ? product.getName().toLowerCase() : "";
        return substrings.stream().allMatch(name::contains);
    }

    private boolean filterByBrandVendorCategoryAndStore(Product product, Set<String> querySubstrings) {
        String name = product.getName() != null ? product.getName().toLowerCase() : "";
        String brand = product.getBrand() != null ? product.getBrand().toLowerCase() : "";
        String vendor = product.getVendor() != null ? product.getVendor().toLowerCase() : "";
        List<String> categoryNames = product.getCategory().stream()
                .map(cat -> cat.getName().toLowerCase())
                .collect(Collectors.toList());
        String store = product.getStore() != null ? product.getStore().getName().toLowerCase() : "";

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

        return !nonNameSubstrings.isEmpty() && nameMatches;
    }

    private Map<String, Object> convertProductToMap(Product product) {
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("ean", product.getEan());
        productMap.put("name", product.getName());
        productMap.put("brand", product.getBrand());
        productMap.put("vendor", product.getVendor());
        productMap.put("current_price", product.getCurrentPrice());
        productMap.put("store", product.getStore() != null ? product.getStore().getName() : null);
        return productMap;
    }

    public Map<String, List<Product>> findMatches(List<Map<String, String>> ingredients) {
        Map<String, List<Product>> result = new HashMap<>();

        for (Map<String, String> ingredient : ingredients) {
            String ingredientName = ingredient.get("ingredient");

            // Query MongoDB for products that match the ingredient name
            List<Product> matchedProducts = productRepository.findByNameIgnoreCase(ingredientName);

            result.put(ingredientName, matchedProducts);
        }

        return result;
    }
}

