package com.example.tasterj.model;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@Setter
public class Product {

    private String id;
    private String ean;
    private String name;
    private String brand;
    private String vendor;
    private String url;
    private String image;
    private List<Category> category;
    private String description;
    private double currentPrice;
    private double currentUnitPrice;
    private double weight;
    private String weightUnit;
    private Store store;

    @Getter
    @Setter
    public static class Category {
        private int id;
        private int depth;
        private String name;
    }

    @Getter
    @Setter
    public static class Store {
        private String name;
        private String code;
        private String url;
        private String logo;
    }
}
