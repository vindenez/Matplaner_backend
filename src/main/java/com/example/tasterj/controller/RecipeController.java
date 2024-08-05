package com.example.tasterj.controller;

import com.example.tasterj.dto.CreateRecipeDto;
import com.example.tasterj.dto.UpdateRecipeDto;
import com.example.tasterj.model.Recipe;
import com.example.tasterj.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @GetMapping
    public ResponseEntity<Page<Recipe>> getRecipes(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipes = recipeService.getRecipes(pageable);
        return new ResponseEntity<>(recipes, HttpStatus.OK);
    }

    @GetMapping("/favorites")
    public ResponseEntity<Page<Recipe>> getFavoriteRecipes(@RequestHeader("userId") String userId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipes = recipeService.getFavoriteRecipes(userId, pageable);
        return new ResponseEntity<>(recipes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable String id) {
        Recipe recipe = recipeService.getRecipeById(id);
        return new ResponseEntity<>(recipe, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@RequestHeader("userId") String userId, @Valid @RequestBody CreateRecipeDto createRecipeDto) {
        Recipe createdRecipe = recipeService.createRecipe(userId, createRecipeDto);
        return new ResponseEntity<>(createdRecipe, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Recipe> updateRecipe(@PathVariable String id, @Valid @RequestBody UpdateRecipeDto updateRecipeDto) {
        Recipe updatedRecipe = recipeService.updateRecipe(id, updateRecipeDto);
        return new ResponseEntity<>(updatedRecipe, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable String id) {
        recipeService.deleteRecipe(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
