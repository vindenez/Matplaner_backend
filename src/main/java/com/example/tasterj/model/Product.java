package com.example.tasterj.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonProperty("current_price")
    private double currentPrice;

    @JsonProperty("current_unit_price")
    private double currentUnitPrice;

    private double weight;

    @JsonProperty("weight_unit")
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
