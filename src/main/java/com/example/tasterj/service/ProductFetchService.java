package com.example.tasterj.service;

import com.example.tasterj.model.Product;
import com.example.tasterj.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductFetchService {

    @Autowired
    private ProductRepository productRepository;

    public Product getProductByEan(String ean) {
        return productRepository.findByEan(ean);
    }

    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContaining(name);
    }

    public List<Product> getProductsByStore(String storeCode) {
        return productRepository.findByStoreCode(storeCode);
    }
}

