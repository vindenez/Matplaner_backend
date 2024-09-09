package com.example.tasterj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.tasterj.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
}

