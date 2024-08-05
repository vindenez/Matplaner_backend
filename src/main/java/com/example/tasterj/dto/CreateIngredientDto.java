package com.example.tasterj.dto;

import com.example.tasterj.model.IngredientUnit;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class CreateIngredientDto {

    @JsonProperty("ean")
    @Size(max = 63)
    private String ean;

    @JsonProperty("amount")
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal amount;

    @JsonProperty("name")
    @NotNull
    @Size(min = 1, max = 255)
    private String name;

    @JsonProperty("image")
    @Size(max = 2083)
    @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$")
    private String image;

    @JsonProperty("unit")
    @NotNull
    private IngredientUnit unit;

    // Getters and Setters
    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public IngredientUnit getUnit() {
        return unit;
    }

    public void setUnit(IngredientUnit unit) {
        this.unit = unit;
    }
}
