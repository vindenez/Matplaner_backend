package com.example.tasterj.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class UpdateRecipeDto {

    @JsonProperty("name")
    @Size(min = 1, max = 255)
    private String name;

    @JsonProperty("description")
    @Size(min = 1, max = 255)
    private String description;

    @JsonProperty("instructions")
    @Size(min = 1, max = 2083)
    private String instructions;

    @JsonProperty("tags")
    private List<@Size(min = 1, max = 255) String> tags;

    @JsonProperty("ingredients")
    private List<@Valid CreateIngredientDto> ingredients;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<CreateIngredientDto> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<CreateIngredientDto> ingredients) {
        this.ingredients = ingredients;
    }
}

