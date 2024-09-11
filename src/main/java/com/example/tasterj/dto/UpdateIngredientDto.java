package com.example.tasterj.dto;

import com.example.tasterj.model.IngredientUnit;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateIngredientDto {

    @JsonProperty("ean")
    @Size(max = 63)
    private String ean;

    @JsonProperty("ean")
    @Size(max = 63)
    private String storeCode;

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

}
