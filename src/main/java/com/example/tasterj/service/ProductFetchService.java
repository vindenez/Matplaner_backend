package com.example.tasterj.service;

import com.example.tasterj.model.Product;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;

@Service
public class ProductFetchService {

    @Autowired
    private MongoClient mongoClient;

    @Value("${mongodb.database}")
    private String databaseName;

    @Value("${mongodb.collection}")
    private String collectionName;

    private MongoCollection<Product> getProductCollection() {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        return database.getCollection(collectionName, Product.class);
    }

}
