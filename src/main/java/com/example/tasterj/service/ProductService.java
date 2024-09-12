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
        this.collection = mongoClient.getDatabase("products").getCollection("products_collection");  // Initialize the collection
    }


    public Map<String, Object> searchProducts(String query, List<String> selectedStores, int page, int pageSize) {
        // Step 1: Create MongoDB filters
        List<Bson> filters = new ArrayList<>();

        if (query != null && !query.isEmpty()) {
            Bson nameFilter = Filters.regex("name", ".*" + query + ".*", "i"); // Case-insensitive name match
            filters.add(nameFilter);
        }

        if (selectedStores != null && !selectedStores.isEmpty()) {
            Bson storeFilter = Filters.in("store.name", selectedStores);
            filters.add(storeFilter);
        }

        // Step 2: Combine filters if any exist
        Bson combinedFilters = filters.isEmpty() ? new Document() : Filters.and(filters);

        // Step 3: Query MongoDB with filters and implement pagination
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

        // Step 4: Count total products that match the filters
        long totalProducts = collection.countDocuments(combinedFilters);

        // Step 5: Convert filtered products to Map<String, Object>
        List<Map<String, Object>> productMaps = filteredProducts.stream()
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

    public List<Product> fetchProductsForIngredients(List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(ingredient -> productRepository.findByEanAndStoreCode(ingredient.getEan(), ingredient.getStoreCode()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
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
}
