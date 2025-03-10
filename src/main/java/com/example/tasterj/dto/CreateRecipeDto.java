package com.example.tasterj.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CreateRecipeDto {

    @JsonProperty("name")
    @NotNull
    @Size(min = 1, max = 255)
    private String name;

    @JsonProperty("description")
    @NotNull
    @Size(min = 1, max = 255)
    private String description;

    @JsonProperty("instructions")
    @NotNull
    @Size(min = 1, max = 2083)
    private String instructions;

    @JsonProperty("tags")
    @NotNull
    @Size(min = 1)
    private List<@NotNull @Size(min = 1, max = 255) String> tags;

    @JsonProperty("ingredients")
    @NotNull
    @Size(min = 1)
    private List<@Valid CreateIngredientDto> ingredients;

    @JsonProperty("userId")
    @NotNull
    private String userId;

    @JsonProperty("imageUrl")
    private String imageUrl;

    @JsonProperty("currentPrice")
    private double currentPrice;

    @JsonProperty("storedPrice")
    private double storedPrice;

    @JsonProperty("priceLastUpdated")
    private LocalDateTime priceLastUpdated;

    @JsonProperty("isPublic")
    private boolean isPublic;

}
