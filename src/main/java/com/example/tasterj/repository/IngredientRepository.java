package com.example.tasterj.repository;


import com.example.tasterj.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, String> {
    void deleteByRecipeId(String recipeId);

    List<Ingredient> findByRecipeId(String recipeId);
}

