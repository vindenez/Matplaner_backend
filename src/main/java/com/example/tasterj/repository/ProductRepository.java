package com.example.tasterj.repository;

import com.example.tasterj.model.Product;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.*;

@Repository
public class ProductRepository {

    private final MongoCollection<Document> collection;

    @Autowired
    public ProductRepository(MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase("products");
        this.collection = database.getCollection("products_collection");
    }

    // Find a product by its EAN
    public Optional<Product> findByEan(String ean) {
        Document document = collection.find(and(eq("ean", ean), gte("current_price", 1.0))).first();
        return Optional.ofNullable(document).map(this::documentToProduct);
    }

    // Find all products that contain a specific keyword in their name, filtering out products with price below 1kr
    public List<Product> findByNameContaining(String name) {
        List<Product> products = new ArrayList<>();
        for (Document document : collection.find(and(regex("name", name, "i"), gte("current_price", 1.0)))) {
            products.add(documentToProduct(document));
        }
        return products;
    }

    // Find all products from a specific store by store code, filtering out products with price below 1kr
    public List<Product> findByStoreCode(String storeCode) {
        List<Product> products = new ArrayList<>();
        for (Document document : collection.find(and(eq("store.code", storeCode), gte("current_price", 1.0)))) {
            products.add(documentToProduct(document));
        }
        return products;
    }

    // Find a product by EAN and store code, filtering out products with price below 1kr
    public Optional<Product> findByEanAndStoreCode(String ean, String storeCode) {
        Document document = collection.find(and(eq("ean", ean), eq("store.code", storeCode), gte("current_price", 1.0))).first();
        return Optional.ofNullable(document).map(this::documentToProduct);
    }

    // Utility: Convert a MongoDB Document to a Product object
    public Product documentToProduct(Document document) {
        Product product = new Product();

        Object idField = document.get("_id");
        if (idField instanceof ObjectId) {
            product.setId(((ObjectId) idField).toHexString());
        } else if (idField instanceof String) {
            product.setId((String) idField);
        }

        product.setEan(document.getString("ean"));
        product.setName(document.getString("name"));

        product.setBrand(document.getString("brand") != null ? document.getString("brand") : "");
        product.setVendor(document.getString("vendor") != null ? document.getString("vendor") : "");
        product.setUrl(document.getString("url") != null ? document.getString("url") : "");
        product.setImage(document.getString("image") != null ? document.getString("image") : "");
        product.setDescription(document.getString("description") != null ? document.getString("description") : "");

        Double currentPrice = document.getDouble("current_price");
        product.setCurrentPrice(currentPrice != null ? currentPrice : 0.0);

        Double currentUnitPrice = document.getDouble("current_unit_price");
        product.setCurrentUnitPrice(currentUnitPrice != null ? currentUnitPrice : 0.0);

        product.setWeight(document.getDouble("weight") != null ? document.getDouble("weight") : 0.0);
        product.setWeightUnit(document.getString("weight_unit") != null ? document.getString("weight_unit") : "");

        Document storeDoc = document.get("store", Document.class);
        if (storeDoc != null) {
            Product.Store store = new Product.Store();
            store.setName(storeDoc.getString("name") != null ? storeDoc.getString("name") : "");
            store.setCode(storeDoc.getString("code") != null ? storeDoc.getString("code") : "");
            store.setUrl(storeDoc.getString("url") != null ? storeDoc.getString("url") : "");
            store.setLogo(storeDoc.getString("logo") != null ? storeDoc.getString("logo") : "");
            product.setStore(store);
        }

        List<Document> categoryDocs = document.getList("category", Document.class);
        if (categoryDocs != null) {
            List<Product.Category> categories = new ArrayList<>();
            for (Document categoryDoc : categoryDocs) {
                Product.Category category = new Product.Category();
                category.setId(categoryDoc.getInteger("id") != null ? categoryDoc.getInteger("id") : 0);
                category.setDepth(categoryDoc.getInteger("depth") != null ? categoryDoc.getInteger("depth") : 0);
                category.setName(categoryDoc.getString("name") != null ? categoryDoc.getString("name") : "");
                categories.add(category);
            }
            product.setCategory(categories);
        }

        return product;
    }


}
