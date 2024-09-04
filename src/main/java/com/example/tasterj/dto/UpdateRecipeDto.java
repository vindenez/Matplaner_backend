package com.example.tasterj.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateRecipeDto {

    // Getters and Setters
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

    @JsonProperty("imageUrl")
    private String imageUrl;

}

