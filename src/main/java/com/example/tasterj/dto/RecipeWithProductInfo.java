package com.example.tasterj.dto;

import com.example.tasterj.model.Recipe;

import java.util.List;
import java.util.Map;

public class RecipeWithProductInfo {

    private Recipe recipe;
    private List<Map<String, Object>> productInfo;

    public RecipeWithProductInfo(Recipe recipe, List<Map<String, Object>> productInfo) {
        this.recipe = recipe;
        this.productInfo = productInfo;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public List<Map<String, Object>> getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(List<Map<String, Object>> productInfo) {
        this.productInfo = productInfo;
    }
}

