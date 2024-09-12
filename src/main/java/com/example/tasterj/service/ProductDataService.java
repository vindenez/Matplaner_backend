package com.example.tasterj.service;

import com.example.tasterj.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ProductDataService {

    @Autowired
    private MongoClient mongoClient;

    @Value("${kassalapp.url}")
    private String API_KEY;

    private static final String PRODUCT_URL = "https://kassal.app/api/v1/products";
    private static final int PAGE_LIMIT = 1000;
    private static final int RATE_LIMIT = 60;
    private static final int BATCH_SIZE = 1000;

    private final String databaseName = "products";

    private final String collectionName = "products_collection";



    // On startup, fetch and save products
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationStartup() {
        fetchAndSaveProducts();
    }

    // Scheduled to run every day at 7 AM
    @Scheduled(cron = "0 0 7 * * ?")
    public void scheduledFetchAndSaveProducts() {
        fetchAndSaveProducts();
    }

    // Fetch products and save to MongoDB
    public void fetchAndSaveProducts() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + API_KEY);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            List<Product> allProducts = new ArrayList<>();
            int currentPage = 1;

            while (currentPage <= PAGE_LIMIT) {
                for (int i = 0; i < RATE_LIMIT && currentPage <= PAGE_LIMIT; i++, currentPage++) {
                    String productUrl = PRODUCT_URL + "?page=" + currentPage;
                    ResponseEntity<String> response = restTemplate.exchange(productUrl, HttpMethod.GET, entity, String.class);

                    if (response.getStatusCode() == HttpStatus.OK) {
                        String productsJson = response.getBody();
                        List<Product> products = parseProducts(productsJson);
                        allProducts.addAll(products);

                        if (allProducts.size() >= BATCH_SIZE) {
                            saveProductsToMongoDB(allProducts);
                            allProducts.clear();
                        }
                    } else {
                        System.err.println("Failed to fetch page " + currentPage + ": " + response.getStatusCode());
                    }
                }

                if (currentPage <= PAGE_LIMIT) {
                    System.out.println("Pausing for 1 minute to respect rate limits...");
                    TimeUnit.MINUTES.sleep(1);
                }
            }

            if (!allProducts.isEmpty()) {
                saveProductsToMongoDB(allProducts);
            }

            System.out.println("All products fetched and saved to MongoDB.");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error fetching or saving products: " + e.getMessage());
        }
    }

    // Method to save products to MongoDB
    private void saveProductsToMongoDB(List<Product> products) {
        MongoCollection<Product> collection = getProductCollection();
        collection.insertMany(products);  // Directly insert the Product objects
        System.out.println(products.size() + " products saved to MongoDB.");
    }

    // Method to get MongoDB collection
    private MongoCollection<Product> getProductCollection() {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        return database.getCollection(collectionName, Product.class);
    }

    // Method to parse product JSON to a list of Product objects
    private List<Product> parseProducts(String productsJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return Arrays.asList(objectMapper.readValue(productsJson, Product[].class));
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
