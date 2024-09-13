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
        Set<String> querySubstrings = new HashSet<>(generateSubstrings(query));

        // Step 1: Create MongoDB filters for name, brand, vendor, store, and category
        List<Bson> filters = new ArrayList<>();

        // Filter by product name using regex for partial substring match
        if (query != null && !query.isEmpty()) {
            List<Bson> nameOrBrandFilters = new ArrayList<>();

            // Check if product name or brand matches any of the substrings
            for (String substring : querySubstrings) {
                nameOrBrandFilters.add(Filters.regex("name", ".*" + substring + ".*", "i")); // Case-insensitive match
                nameOrBrandFilters.add(Filters.regex("brand", ".*" + substring + ".*", "i")); // Case-insensitive match
                nameOrBrandFilters.add(Filters.regex("vendor", ".*" + substring + ".*", "i"));
                nameOrBrandFilters.add(Filters.regex("category.name", ".*" + substring + ".*", "i"));
            }

            filters.add(Filters.or(nameOrBrandFilters)); // Match any condition for name, brand, or category
        }

        // Step 2: Filter by selected stores if applicable
        if (selectedStores != null && !selectedStores.isEmpty()) {
            filters.add(Filters.in("store.name", selectedStores));
        }

        // Step 3: Combine filters if any exist
        Bson combinedFilters = filters.isEmpty() ? new Document() : Filters.and(filters);

        // Step 4: Query MongoDB with filters and implement pagination
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

        // Step 5: Count total products that match the filters
        long totalProducts = collection.countDocuments(combinedFilters);

        // Step 6: Convert filtered products to Map<String, Object>
        List<Map<String, Object>> productMaps = filteredProducts.stream()
                .map(this::convertProductToMap) // Assuming convertProductToMap returns the entire product information
                .collect(Collectors.toList());

        // Step 7: Create response with paginated products and metadata
        Map<String, Object> response = new HashMap<>();
        response.put("products", productMaps);
        response.put("totalItems", totalProducts);
        response.put("totalPages", (int) Math.ceil((double) totalProducts / pageSize));
        response.put("currentPage", page);

        return response;
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
