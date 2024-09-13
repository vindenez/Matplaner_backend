package com.example.tasterj.repository;

import com.example.tasterj.model.Product;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
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
        Document document = collection.find(eq("ean", ean)).first();
        return Optional.ofNullable(document).map(this::documentToProduct);
    }

    // Find all products that contain a specific keyword in their name
    public List<Product> findByNameContaining(String name) {
        List<Product> products = new ArrayList<>();
        for (Document document : collection.find(regex("name", name, "i"))) {
            products.add(documentToProduct(document));
        }
        return products;
    }

    // Find all products from a specific store by store code
    public List<Product> findByStoreCode(String storeCode) {
        List<Product> products = new ArrayList<>();
        for (Document document : collection.find(eq("store.code", storeCode))) {
            products.add(documentToProduct(document));
        }
        return products;
    }

    // Find a product by EAN and store code
    public Optional<Product> findByEanAndStoreCode(String ean, String storeCode) {
        Document document = collection.find(and(eq("ean", ean), eq("store.code", storeCode))).first();
        return Optional.ofNullable(document).map(this::documentToProduct);
    }


    // Utility: Convert a MongoDB Document to a Product object
    public Product documentToProduct(Document document) {
        Product product = new Product();

        product.setId(document.getObjectId("_id").toHexString());
        product.setEan(document.getString("ean"));
        product.setName(document.getString("name"));
        product.setBrand(document.getString("brand"));
        product.setVendor(document.getString("vendor"));
        product.setUrl(document.getString("url"));
        product.setImage(document.getString("image"));
        product.setDescription(document.getString("description"));
        product.setCurrentPrice(document.getDouble("current_price"));
        product.setCurrentUnitPrice(document.getDouble("current_unit_price"));
        product.setWeight(document.getDouble("weight"));
        product.setWeightUnit(document.getString("weight_unit"));

        Document storeDoc = document.get("store", Document.class);
        if (storeDoc != null) {
            Product.Store store = new Product.Store();
            store.setName(storeDoc.getString("name"));
            store.setCode(storeDoc.getString("code"));
            store.setUrl(storeDoc.getString("url"));
            store.setLogo(storeDoc.getString("logo"));
            product.setStore(store);
        }

        List<Document> categoryDocs = document.getList("category", Document.class);
        if (categoryDocs != null) {
            List<Product.Category> categories = new ArrayList<>();
            for (Document categoryDoc : categoryDocs) {
                Product.Category category = new Product.Category();
                category.setId(categoryDoc.getInteger("id"));
                category.setDepth(categoryDoc.getInteger("depth"));
                category.setName(categoryDoc.getString("name"));
                categories.add(category);
            }
            product.setCategory(categories);
        }

        return product;
    }
}
