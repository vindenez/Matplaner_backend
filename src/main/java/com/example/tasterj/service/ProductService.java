package com.example.tasterj.service;

import com.example.tasterj.dto.CreateRecipeDto;
import com.example.tasterj.model.Ingredient;
import com.example.tasterj.model.Product;
import com.example.tasterj.model.Recipe;
import com.example.tasterj.repository.ProductRepository;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    private final MongoCollection<Document> collection;

    @Autowired
    public ProductService(ProductRepository productRepository, MongoClient mongoClient) {
        this.productRepository = productRepository;
        this.collection = mongoClient.getDatabase("products").getCollection("products_collection");
    }


    public Map<String, Object> searchProducts(String query, List<String> selectedStores, int page, int pageSize) {
        if (query == null || query.isEmpty()) {
            // Handle empty query case
            Map<String, Object> response = new HashMap<>();
            response.put("products", Collections.emptyList());
            response.put("totalItems", 0);
            response.put("totalPages", 0);
            response.put("currentPage", page);
            return response;
        }

        // Step 1: Split query into words
        String[] words = query.toLowerCase().split("\\s+");

        // Step 2: Generate all possible substrings
        List<String> substrings = generateSubstrings(words);

        // Step 3: Use MongoDB to find brands that match any of the substrings
        List<Bson> brandFilters = substrings.stream()
                .map(sub -> Filters.regex("brand", Pattern.compile("^" + Pattern.quote(sub) + "$", Pattern.CASE_INSENSITIVE)))
                .collect(Collectors.toList());

        Bson brandQuery = Filters.or(brandFilters);

        // Find matching brands
        List<String> matchingBrands = collection.distinct("brand", brandQuery, String.class)
                .into(new ArrayList<>());

        // Step 4: Identify the longest matching brand substring
        String matchingBrand = null;
        int maxBrandLength = 0;

        for (String brand : matchingBrands) {
            String brandLower = brand.toLowerCase();
            if (substrings.contains(brandLower)) {
                int length = brandLower.split("\\s+").length;
                if (length > maxBrandLength) {
                    maxBrandLength = length;
                    matchingBrand = brand;
                }
            }
        }

        // Step 5: Remove brand words from query words
        List<String> remainingWords = new ArrayList<>(Arrays.asList(words));
        if (matchingBrand != null) {
            String[] brandWords = matchingBrand.toLowerCase().split("\\s+");
            for (String word : brandWords) {
                remainingWords.remove(word);
            }
        }

        // Step 6: Create filters that check both product names and categories
        List<Bson> wordFilters = new ArrayList<>();

        for (String word : remainingWords) {
            List<Bson> orFilters = new ArrayList<>();
            orFilters.add(Filters.regex("name", Pattern.compile(Pattern.quote(word), Pattern.CASE_INSENSITIVE)));
            orFilters.add(Filters.regex("category.name", Pattern.compile(Pattern.quote(word), Pattern.CASE_INSENSITIVE)));
            wordFilters.add(Filters.or(orFilters));
        }

        // Step 7: Combine filters
        List<Bson> filters = new ArrayList<>();

        if (!wordFilters.isEmpty()) {
            filters.add(Filters.and(wordFilters));
        }

        if (matchingBrand != null) {
            filters.add(Filters.regex("brand", Pattern.compile(Pattern.quote(matchingBrand), Pattern.CASE_INSENSITIVE)));
        }

        // Filter by selected stores if applicable
        if (selectedStores != null && !selectedStores.isEmpty()) {
            filters.add(Filters.in("store.name", selectedStores));
        }

        Bson combinedFilters = filters.isEmpty() ? new Document() : Filters.and(filters);

        // Query the database with pagination
        List<Product> filteredProducts = new ArrayList<>();
        MongoCursor<Document> cursor = collection.find(combinedFilters)
                .skip((page - 1) * pageSize)
                .limit(pageSize)
                .iterator();

        try {
            while (cursor.hasNext()) {
                filteredProducts.add(productRepository.documentToProduct(cursor.next()));
            }
        } finally {
            cursor.close();
        }

        // Count total products that match the filters
        long totalProducts = collection.countDocuments(combinedFilters);

        // Convert filtered products to Map<String, Object>
        List<Map<String, Object>> productMaps = filteredProducts.stream()
                .map(this::convertProductToMap)
                .collect(Collectors.toList());

        // Create response with paginated products and metadata
        Map<String, Object> response = new HashMap<>();
        response.put("products", productMaps);
        response.put("totalItems", totalProducts);
        response.put("totalPages", (int) Math.ceil((double) totalProducts / pageSize));
        response.put("currentPage", page);

        return response;
    }


    private List<String> generateSubstrings(String[] words) {
        List<String> substrings = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < words.length; j++) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(words[j]);
                substrings.add(sb.toString());
            }
        }
        return substrings;
    }


    public Map<String, List<Map<String, Object>>> findMatches(List<Map<String, String>> ingredients) {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();

        for (Map<String, String> ingredient : ingredients) {
            String ingredientName = ingredient.get("ingredient");

            List<Product> matchedProducts = productRepository.findByNameContaining(ingredientName);

            List<Map<String, Object>> productMaps = matchedProducts.stream()
                    .map(this::convertProductToMap)
                    .collect(Collectors.toList());

            result.put(ingredientName, productMaps);
        }

        return result;
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


    public List<Product> fetchProductsForIngredients(List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(ingredient -> productRepository.findByEanAndStoreCode(ingredient.getEan(), ingredient.getStoreCode()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertProductToMap(Product product) {
        Map<String, Object> productMap = new HashMap<>();

        productMap.put("id", product.getId());
        productMap.put("ean", product.getEan());
        productMap.put("name", product.getName());
        productMap.put("brand", product.getBrand());
        productMap.put("vendor", product.getVendor());
        productMap.put("url", product.getUrl());
        productMap.put("image", product.getImage());
        productMap.put("description", product.getDescription());
        productMap.put("current_price", product.getCurrentPrice());
        productMap.put("current_unit_price", product.getCurrentUnitPrice());
        productMap.put("weight", product.getWeight());
        productMap.put("weight_unit", product.getWeightUnit());

        if (product.getStore() != null) {
            Map<String, Object> storeMap = new HashMap<>();
            storeMap.put("name", product.getStore().getName());
            storeMap.put("code", product.getStore().getCode());
            storeMap.put("url", product.getStore().getUrl());
            storeMap.put("logo", product.getStore().getLogo());

            productMap.put("store", storeMap);
        }

        if (product.getCategory() != null && !product.getCategory().isEmpty()) {
            List<Map<String, Object>> categoryList = new ArrayList<>();
            for (Product.Category category : product.getCategory()) {
                Map<String, Object> categoryMap = new HashMap<>();
                categoryMap.put("id", category.getId());
                categoryMap.put("depth", category.getDepth());
                categoryMap.put("name", category.getName());
                categoryList.add(categoryMap);
            }
            productMap.put("category", categoryList);
        }

        return productMap;
    }

}
