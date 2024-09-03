package com.example.tasterj.controller;

import com.example.tasterj.dto.CreateRecipeDto;
import com.example.tasterj.dto.UpdateRecipeDto;
import com.example.tasterj.model.Recipe;
import com.example.tasterj.model.User;
import com.example.tasterj.service.RecipeService;
import com.example.tasterj.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<Page<Recipe>> getRecipes(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipes = recipeService.getRecipes(pageable);
        return new ResponseEntity<>(recipes, HttpStatus.OK);
    }

    @GetMapping("/favorites")
    public ResponseEntity<Page<Recipe>> getFavoriteRecipes(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size,
                                                           Principal principal) {
        Pageable pageable = PageRequest.of(page, size);
        String supabaseUserId = principal.getName();
        Page<Recipe> recipes = recipeService.getFavoriteRecipes(supabaseUserId, pageable);
        return new ResponseEntity<>(recipes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable String id) {
        Recipe recipe = recipeService.getRecipeById(id);
        return new ResponseEntity<>(recipe, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Recipe> createRecipe(@Valid @RequestBody CreateRecipeDto createRecipeDto) {
        String userId = createRecipeDto.getUserId();
        User user = userService.getUserBySupabaseUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Recipe recipe = recipeService.createRecipe(user.getSupabaseUserId(), createRecipeDto);
        return new ResponseEntity<>(recipe, HttpStatus.CREATED);
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
