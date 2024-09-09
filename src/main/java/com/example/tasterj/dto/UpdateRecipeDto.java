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

    @JsonProperty("name")
    @Size(max = 255)
    private String name;

    @JsonProperty("description")
    @Size(max = 255)
    private String description;

    @JsonProperty("instructions")
    @Size(max = 2083)
    private String instructions;

    @JsonProperty("tags")
    private List<@Size(max = 255) String> tags;

    @JsonProperty("ingredients")
    private List<@Valid CreateIngredientDto> ingredients;

    @JsonProperty("imageUrl")
    private String imageUrl;

}

