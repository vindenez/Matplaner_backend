package com.example.tasterj.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.example.tasterj.model.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
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
    private static final int BATCH_SIZE = 1000;

    private final String databaseName = "products";
    private final String collectionName = "products_collection";

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationStartup() {
        if (fetchOnStartup) {
            fetchAndSaveProducts();
        }
    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void scheduledFetchAndSaveProducts() {
        fetchAndSaveProducts();
    }

    public void fetchAndSaveProducts() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + API_KEY);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            List<Product> allProducts = new ArrayList<>();
            int currentPage = 1;
            boolean hasMoreProducts = true;

            while (hasMoreProducts) {
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

                        if (allProducts.size() >= BATCH_SIZE) {
                            saveProductsToMongoDB(allProducts);
                            allProducts.clear();
                        }
                    } else {
                        System.err.println("Failed to fetch page " + currentPage + ": " + response.getStatusCode());
                        hasMoreProducts = false;
                    }
                }

                if (hasMoreProducts) {
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

    private void saveProductsToMongoDB(List<Product> products) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Product> collection = database.getCollection(collectionName, Product.class);

        for (Product product : products) {
            collection.replaceOne(
                    new Document("ean", product.getEan()).append("store.code", product.getStore().getCode()),
                    product,
                    new com.mongodb.client.model.ReplaceOptions().upsert(true)
            );
        }

        System.out.println(products.size() + " products saved/updated to MongoDB.");
    }

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
}
