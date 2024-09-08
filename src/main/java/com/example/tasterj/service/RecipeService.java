package com.example.tasterj.service;

import com.example.tasterj.dto.CreateRecipeDto;
import com.example.tasterj.dto.UpdateRecipeDto;
import com.example.tasterj.model.Recipe;
import com.example.tasterj.model.Ingredient;
import com.example.tasterj.model.SavedRecipe;
import com.example.tasterj.model.User;
import com.example.tasterj.repository.RecipeRepository;
import com.example.tasterj.repository.IngredientRepository;
import com.example.tasterj.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.example.tasterj.repository.SavedRecipeRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    @Autowired
    private SavedRecipeRepository savedRecipeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private ImageService imageService;

    public Page<Recipe> getRecipes(Pageable pageable) {
        return recipeRepository.findAll(pageable);
    }

    public Recipe getRecipeById(String id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
    }

    @Transactional
    public Recipe createRecipe(String userId, CreateRecipeDto createRecipeDto) {
        Recipe recipe = new Recipe();
        recipe.setId(UUID.randomUUID().toString());
        recipe.setUserId(userId);
        recipe.setName(createRecipeDto.getName());
        recipe.setDescription(createRecipeDto.getDescription());
        recipe.setInstructions(createRecipeDto.getInstructions());
        recipe.setTags(createRecipeDto.getTags());

        recipe.setIngredients(createRecipeDto.getIngredients().stream().map(dto -> {
            Ingredient ingredient = new Ingredient();
            ingredient.setId(UUID.randomUUID().toString());
            ingredient.setRecipe(recipe);
            ingredient.setName(dto.getName());
            ingredient.setAmount(dto.getAmount());
            ingredient.setUnit(dto.getUnit());
            ingredient.setEan(dto.getEan());
            ingredient.setImage(dto.getImage());
            return ingredient;
        }).collect(Collectors.toList()));

        if (createRecipeDto.getImageUrl() != null && !createRecipeDto.getImageUrl().isEmpty()) {
            recipe.setImageUrl(createRecipeDto.getImageUrl());
        }

        return recipeRepository.save(recipe);
    }

    @Transactional
    public Recipe updateRecipe(String id, UpdateRecipeDto updateRecipeDto) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));

        if (updateRecipeDto.getName() != null) {
            recipe.setName(updateRecipeDto.getName());
        }
        if (updateRecipeDto.getDescription() != null) {
            recipe.setDescription(updateRecipeDto.getDescription());
        }
        if (updateRecipeDto.getInstructions() != null) {
            recipe.setInstructions(updateRecipeDto.getInstructions());
        }
        if (updateRecipeDto.getTags() != null) {
            recipe.setTags(updateRecipeDto.getTags());
        }

        if (updateRecipeDto.getIngredients() != null) {
            ingredientRepository.deleteByRecipeId(recipe.getId());
            recipe.setIngredients(updateRecipeDto.getIngredients().stream().map(dto -> {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(UUID.randomUUID().toString());
                ingredient.setRecipe(recipe);
                ingredient.setName(dto.getName());
                ingredient.setAmount(dto.getAmount());
                ingredient.setUnit(dto.getUnit());
                ingredient.setEan(dto.getEan());
                ingredient.setImage(dto.getImage());
                return ingredient;
            }).collect(Collectors.toList()));
        }


        if (updateRecipeDto.getImageUrl() != null && !updateRecipeDto.getImageUrl().isEmpty()) {
            recipe.setImageUrl(updateRecipeDto.getImageUrl());
        }

        return recipeRepository.save(recipe);
    }

    @Transactional
    public void deleteRecipe(String id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        recipeRepository.delete(recipe);
    }

    public boolean saveRecipeForUser(String userId, String recipeId) {
        Optional<User> userOpt = userRepository.findById(Long.parseLong(userId));
        Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);

        if (userOpt.isPresent() && recipeOpt.isPresent()) {
            User user = userOpt.get();
            Recipe recipe = recipeOpt.get();

            // Check if the user has already saved the recipe
            if (savedRecipeRepository.findByUserAndRecipe(user, recipe).isEmpty()) {
                SavedRecipe savedRecipe = new SavedRecipe();
                savedRecipe.setUser(user);
                savedRecipe.setRecipe(recipe);

                savedRecipeRepository.save(savedRecipe);
                return true;
            }
        }
        return false;
    }

    public boolean removeSavedRecipeForUser(String userId, String recipeId) {
        Optional<User> userOpt = userRepository.findById(Long.parseLong(userId));
        Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);

        if (userOpt.isPresent() && recipeOpt.isPresent()) {
            User user = userOpt.get();
            Recipe recipe = recipeOpt.get();

            Optional<SavedRecipe> savedRecipeOpt = savedRecipeRepository.findByUserAndRecipe(user, recipe);

            if (savedRecipeOpt.isPresent()) {
                savedRecipeRepository.deleteByUserAndRecipe(user, recipe);
                return true;
            }
        }
        return false;
    }

    public List<SavedRecipe> getSavedRecipesForUser(String userId) {
        Optional<User> userOpt = userRepository.findById(Long.parseLong(userId));
        return userOpt.map(savedRecipeRepository::findByUser).orElse(List.of());
    }

    public boolean isRecipeSavedByUser(String userId, String recipeId) {
        Optional<User> userOpt = userRepository.findById(Long.parseLong(userId));
        Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);

        if (userOpt.isPresent() && recipeOpt.isPresent()) {
            User user = userOpt.get();
            Recipe recipe = recipeOpt.get();
            return savedRecipeRepository.findByUserAndRecipe(user, recipe).isPresent();
        }
        return false;
    }

    @Transactional
    public Recipe saveRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }

}
