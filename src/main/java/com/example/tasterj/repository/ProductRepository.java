package com.example.tasterj.repository;

import com.example.tasterj.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {

    // Find a product by its EAN (Primary Key)
    Product findByEan(String ean);

    // Find all products that contain a specific keyword in their name
    List<Product> findByNameContaining(String name);

    // Find all products from a specific store by store code
    List<Product> findByStoreCode(String storeCode);

    Optional<Product> findByEanAndStoreCode(String ean, String storeCode);

    List<Product> findByNameIgnoreCase(String name);
}


