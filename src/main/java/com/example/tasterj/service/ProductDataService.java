package com.example.tasterj.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.example.tasterj.model.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ProductDataService {

    @Autowired
    private MongoClient mongoClient;

    @Value("${kassalapp.api}")
    private String API_KEY;

    @Value("${fetch.on.startup}")
    private boolean fetchOnStartup;

    private static final String PRODUCT_URL = "https://kassal.app/api/v1/products";
    private static final int RATE_LIMIT = 60;

    private final String databaseName = "products";
    private final String collectionName = "products_collection";

    // On startup, fetch and save products if fetchOnStartup is true
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationStartup() {
        if (fetchOnStartup) {
            fetchAndSaveProducts();
        }
    }

    // Scheduled to run every day at 6 AM
    @Scheduled(cron = "0 0 6 * * ?")
    public void scheduledFetchAndSaveProducts() {
        migrateIdsToObjectId();
        fetchAndSaveProducts();
    }

    // Fetch products and save to MongoDB
    public void fetchAndSaveProducts() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + API_KEY);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            boolean hasMoreProducts = true;
            int currentPage = 1;

            while (hasMoreProducts) {
                List<Product> allProducts = new ArrayList<>();

                for (int i = 0; i < RATE_LIMIT && hasMoreProducts; i++, currentPage++) {
                    String productUrl = PRODUCT_URL + "?page=" + currentPage + "&size=100";
                    ResponseEntity<String> response = restTemplate.exchange(productUrl, HttpMethod.GET, entity, String.class);

                    if (response.getStatusCode() == HttpStatus.OK) {
                        String productsJson = response.getBody();
                        List<Product> products = parseProducts(productsJson);

                        if (!products.isEmpty()) {
                            allProducts.addAll(products);
                        }

                        if (products.size() < 100) {
                            hasMoreProducts = false;
                        }
                    } else {
                        System.err.println("Failed to fetch page " + currentPage + ": " + response.getStatusCode());
                        hasMoreProducts = false;
                    }
                }

                // Save/Update all products for the current batch
                if (!allProducts.isEmpty()) {
                    saveProductsToMongoDB(allProducts);
                }

                // If there are more products, pause for 1 minute to respect rate limits
                if (hasMoreProducts) {
                    System.out.println("Pausing for 1 minute to respect rate limits...");
                    TimeUnit.MINUTES.sleep(1);
                }
            }

            System.out.println("All products fetched and saved to MongoDB.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error fetching or saving products: " + e.getMessage());
        }
    }

    // Save or update products in MongoDB
    private void saveProductsToMongoDB(List<Product> products) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Product> collection = database.getCollection(collectionName, Product.class);

        for (Product product : products) {
            Document filter = new Document("ean", product.getEan()).append("store.code", product.getStore().getCode());

            Document update = new Document("$set", new Document("name", product.getName())
                    .append("brand", product.getBrand())
                    .append("vendor", product.getVendor())
                    .append("url", product.getUrl())
                    .append("image", product.getImage())
                    .append("category", product.getCategory())
                    .append("description", product.getDescription())
                    .append("current_price", product.getCurrentPrice())
                    .append("current_unit_price", product.getCurrentUnitPrice())
                    .append("weight", product.getWeight())
                    .append("weight_unit", product.getWeightUnit())
                    .append("store", product.getStore()));

            collection.updateOne(filter, update, new com.mongodb.client.model.UpdateOptions().upsert(true));
        }

        System.out.println(products.size() + " products saved/updated in MongoDB.");
    }

    // Parse products from the API response
    private List<Product> parseProducts(String productsJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(productsJson);
            JsonNode dataNode = root.get("data");

            if (dataNode != null && dataNode.isArray()) {
                return objectMapper.convertValue(dataNode, new TypeReference<List<Product>>() {});
            } else {
                System.err.println("No 'data' field found or it's not an array.");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void migrateIdsToObjectId() {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        // Find all documents where _id is a string
        List<Document> productsWithStringIds = collection.find(Filters.type("_id", "string")).into(new ArrayList<>());

        for (Document product : productsWithStringIds) {
            String stringId = product.getString("_id");

            // Generate a new ObjectId from the string _id
            ObjectId objectId = new ObjectId();

            // Update the document to set the new ObjectId as _id
            collection.updateOne(Filters.eq("_id", stringId), Updates.set("_id", objectId));

            // You can also update the rest of the document here if needed
            System.out.println("Updated product _id from string to ObjectId: " + objectId.toHexString());
        }

        System.out.println("Migration of string _id to ObjectId completed.");
    }
}
