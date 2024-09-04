package com.example.tasterj.controller;

import com.example.tasterj.dto.CreateRecipeDto;
import com.example.tasterj.dto.UpdateRecipeDto;
import com.example.tasterj.model.Recipe;
import com.example.tasterj.model.User;
import com.example.tasterj.service.RecipeService;
import com.example.tasterj.service.ImageService;
import com.example.tasterj.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    @GetMapping
    public ResponseEntity<Page<Recipe>> getRecipes(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipes = recipeService.getRecipes(pageable);
        return new ResponseEntity<>(recipes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable String id) {
        Recipe recipe = recipeService.getRecipeById(id);
        return new ResponseEntity<>(recipe, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Recipe> createRecipe(@RequestPart("createRecipeDto") @Valid CreateRecipeDto createRecipeDto) {
        String userId = createRecipeDto.getUserId();
        User user = userService.getUserBySupabaseUserId(userId);

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Recipe recipe = recipeService.createRecipe(user.getSupabaseUserId(), createRecipeDto);

        return new ResponseEntity<>(recipe, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Recipe> updateRecipe(
            @PathVariable String id,
            @RequestPart("updateRecipeDto") @Valid UpdateRecipeDto updateRecipeDto) {

        Recipe updatedRecipe = recipeService.updateRecipe(id, updateRecipeDto);
        return new ResponseEntity<>(updatedRecipe, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable String id) {
        recipeService.deleteRecipe(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/upload-image")
    public ResponseEntity<String> uploadRecipeImage(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {

        Recipe recipe = recipeService.getRecipeById(id);
        if (recipe == null) {
            return new ResponseEntity<>("Recipe not found", HttpStatus.NOT_FOUND);
        }

        String imageUrl = imageService.uploadImage(file);
        recipe.setImageUrl(imageUrl);
        recipeService.saveRecipe(recipe);

        return new ResponseEntity<>(imageUrl, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/delete-image")
    public ResponseEntity<String> deleteRecipeImage(@PathVariable String id) {
        Recipe recipe = recipeService.getRecipeById(id);
        if (recipe == null || recipe.getImageUrl() == null) {
            return new ResponseEntity<>("Recipe or image not found", HttpStatus.NOT_FOUND);
        }

        imageService.deleteImage(recipe.getImageUrl());

        recipe.setImageUrl(null);
        recipeService.saveRecipe(recipe);

        return new ResponseEntity<>("Image deleted successfully", HttpStatus.OK);
    }
}
