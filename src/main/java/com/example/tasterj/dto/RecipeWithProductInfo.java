package com.example.tasterj.dto;

import com.example.tasterj.model.Recipe;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class RecipeWithProductInfo {

    private Recipe recipe;
    private List<Map<String, Object>> productInfo;

    public RecipeWithProductInfo(Recipe recipe, List<Map<String, Object>> productInfo) {
        this.recipe = recipe;
        this.productInfo = productInfo;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

}

