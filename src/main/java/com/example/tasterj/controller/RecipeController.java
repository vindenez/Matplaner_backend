package com.example.tasterj.controller;

import com.example.tasterj.dto.CreateRecipeDto;
import com.example.tasterj.dto.RecipeWithProductInfo;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.tasterj.model.SavedRecipe;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private ImageService imageService;


    @GetMapping("/user-recipes")
    public ResponseEntity<Page<Recipe>> getUserRecipes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = null;

        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            userId = jwtAuth.getTokenAttributes().get("sub").toString(); // Assuming "sub" contains the userId
        }

        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> userRecipes = recipeService.getUserRecipes(userId, pageable);

        if (userRecipes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(userRecipes, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<Recipe>> getRecipes(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipes = recipeService.getRecipes(pageable);
        return new ResponseEntity<>(recipes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeWithProductInfo> getRecipeById(@PathVariable String id,
                                                               @RequestParam(defaultValue = "false") boolean includeProductInfo) {

        RecipeWithProductInfo recipeWithProductInfo = recipeService.getRecipeById(id, includeProductInfo);
        return new ResponseEntity<>(recipeWithProductInfo, HttpStatus.OK);
    }


    // Create a new recipe
    @PostMapping("/create")
    public ResponseEntity<Recipe> createRecipe(@RequestPart("createRecipeDto") @Valid CreateRecipeDto createRecipeDto) {
        Recipe recipe = recipeService.createRecipe(createRecipeDto);
        return new ResponseEntity<>(recipe, HttpStatus.CREATED);
    }


    // Update an existing recipe (only if owned by the user)
    @PatchMapping("/{id}")
    public ResponseEntity<Recipe> updateRecipe(
            @PathVariable String id,
            @RequestPart("updateRecipeDto") @Valid UpdateRecipeDto updateRecipeDto) {

        Recipe updatedRecipe = recipeService.updateRecipe(id, updateRecipeDto);
        return new ResponseEntity<>(updatedRecipe, HttpStatus.OK);
    }

    // Delete a recipe (only if owned by the user)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable String id, @RequestParam String userId) {
        Recipe existingRecipe = recipeService.getRecipeById(id);
        if (existingRecipe == null || !existingRecipe.getUserId().equals(userId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        recipeService.deleteRecipe(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Upload recipe image
    @PostMapping("/upload-image")
    public ResponseEntity<String> uploadRecipeImage(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken)) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        String jwtToken = ((JwtAuthenticationToken) authentication).getToken().getTokenValue();

        if (file == null || file.isEmpty()) {
            return new ResponseEntity<>("No file uploaded", HttpStatus.BAD_REQUEST);
        }

        String imageUrl = imageService.uploadImage(file, jwtToken);

        if (imageUrl == null || imageUrl.isEmpty()) {
            return new ResponseEntity<>("Image upload failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(imageUrl, HttpStatus.OK);
    }


    // Delete recipe image
    @PostMapping("/delete-image")
    public ResponseEntity<String> deleteRecipeImage(@RequestParam("imageUrl") String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return new ResponseEntity<>("No image URL provided", HttpStatus.BAD_REQUEST);
        }

        try {
            imageService.deleteImage(imageUrl);
            return new ResponseEntity<>("Image deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete image: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Save a recipe for the user
    @PostMapping("/save")
    public boolean saveRecipe(@RequestParam String userId, @RequestParam String recipeId) {
        return recipeService.saveRecipeForUser(userId, recipeId);
    }

    // Remove a saved recipe
    @DeleteMapping("/save/remove")
    public boolean removeSavedRecipe(@RequestParam String userId, @RequestParam String recipeId) {
        return recipeService.removeSavedRecipeForUser(userId, recipeId);
    }

    // Fetch saved recipes for a user with pagination
    @GetMapping("/save/list")
    public ResponseEntity<Page<SavedRecipe>> getSavedRecipes(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SavedRecipe> savedRecipes = recipeService.getSavedRecipesForUser(userId, pageable);

        if (savedRecipes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(savedRecipes, HttpStatus.OK);
    }

    // Check if a recipe is saved
    @GetMapping("/save/is-saved")
    public boolean isRecipeSaved(@RequestParam String userId, @RequestParam String recipeId) {
        return recipeService.isRecipeSavedByUser(userId, recipeId);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Recipe>> searchRecipes(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "0") double minPrice,
            @RequestParam(defaultValue = "10000") double maxPrice,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipes = recipeService.searchRecipes(query, 0, minPrice, maxPrice, sortBy, sortDirection, pageable);
        return new ResponseEntity<>(recipes, HttpStatus.OK);
    }

}
