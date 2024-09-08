package com.example.tasterj.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = true, length = 2083)
    private String imageUrl;

    @Column(nullable = true, length = 63)
    private String ean;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "store_name", nullable = true, length = 255)
    private String storeName;

    @Column(name = "store_logo_url", nullable = true, length = 2083)
    private String storeLogoUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

    public Product() {
        this.id = UUID.randomUUID().toString();
    }
}
