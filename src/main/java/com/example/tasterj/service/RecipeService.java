package com.example.tasterj.service;

import com.example.tasterj.dto.CreateRecipeDto;
import com.example.tasterj.dto.RecipeWithProductInfo;
import com.example.tasterj.dto.UpdateRecipeDto;
import com.example.tasterj.exception.ResourceNotFoundException;
import com.example.tasterj.model.*;

import com.example.tasterj.repository.RecipeRepository;
import com.example.tasterj.repository.IngredientRepository;
import com.example.tasterj.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.example.tasterj.repository.SavedRecipeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private ProductDataService productDataService;

    public Page<Recipe> getRecipes(Pageable pageable) {
        return recipeRepository.findAll(pageable);
    }

    @Transactional
    public Recipe getRecipeById(String id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));

        List<Ingredient> ingredients = ingredientRepository.findByRecipeId(id);

        updateRecipePrice(recipe, ingredients);

        return recipe;
    }

    @Transactional
    public RecipeWithProductInfo getRecipeById(String id, boolean includeProductInfo) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));

        List<Ingredient> ingredients = ingredientRepository.findByRecipeId(id);

        updateRecipePrice(recipe, ingredients);

        if (includeProductInfo) {
            List<Map<String, Object>> productInfo = productDataService.getProductsByEANsAndStoreCodes(ingredients);
            return new RecipeWithProductInfo(recipe, productInfo);
        }

        return new RecipeWithProductInfo(recipe, null);
    }

    @Transactional
    public Page<Recipe> getUserRecipes(String userId, Pageable pageable) {
        return recipeRepository.findByUser_SupabaseUserId(userId, pageable);
    }
    @Transactional
    public Recipe createRecipe(CreateRecipeDto createRecipeDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String userId = ((JwtAuthenticationToken) authentication).getTokenAttributes().get("sub").toString();

        Recipe recipe = new Recipe();
        recipe.setId(UUID.randomUUID().toString());
        recipe.setUserId(userId);
        recipe.setName(createRecipeDto.getName());
        recipe.setDescription(createRecipeDto.getDescription());
        recipe.setInstructions(createRecipeDto.getInstructions());
        recipe.setTags(createRecipeDto.getTags());

        List<Map<String, Object>> products = productDataService.getProducts();
        final double[] totalCurrentPrice = {0.0};

        List<Ingredient> ingredients = createRecipeDto.getIngredients().stream().map(dto -> {
            Ingredient ingredient = new Ingredient();
            ingredient.setId(UUID.randomUUID().toString());
            ingredient.setRecipe(recipe);
            ingredient.setName(dto.getName());
            ingredient.setAmount(dto.getAmount());
            ingredient.setUnit(dto.getUnit());
            ingredient.setEan(dto.getEan());
            ingredient.setStoreCode(dto.getStoreCode());
            ingredient.setImage(dto.getImage());

            Map<String, Object> product = findProductByEan(products, dto.getEan());
            if (product != null) {
                Double productPrice = (Double) product.get("current_price");
                if (productPrice != null) {
                    totalCurrentPrice[0] += productPrice;
                }
            }

            return ingredient;
        }).collect(Collectors.toList());

        recipe.setIngredients(ingredients);
        recipe.setCurrentPrice(totalCurrentPrice[0]);
        recipe.setStoredPrice(totalCurrentPrice[0]);
        recipe.setPriceLastUpdated(LocalDateTime.now());

        if (createRecipeDto.getImageUrl() != null && !createRecipeDto.getImageUrl().isEmpty()) {
            recipe.setImageUrl(createRecipeDto.getImageUrl());
        }

        return recipeRepository.save(recipe);
    }


    @Transactional
    public Recipe updateRecipe(String id, UpdateRecipeDto updateRecipeDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String userId = ((JwtAuthenticationToken) authentication).getTokenAttributes().get("sub").toString();
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));

        if (!recipe.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this recipe");
        }

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
            List<Map<String, Object>> products = productDataService.getProducts();
            final double[] totalCurrentPrice = {0.0};

            List<Ingredient> updatedIngredients = updateRecipeDto.getIngredients().stream().map(dto -> {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(UUID.randomUUID().toString());
                ingredient.setRecipe(recipe);
                ingredient.setName(dto.getName());
                ingredient.setAmount(dto.getAmount());
                ingredient.setUnit(dto.getUnit());
                ingredient.setEan(dto.getEan());
                ingredient.setStoreCode(dto.getStoreCode());
                ingredient.setImage(dto.getImage());

                Map<String, Object> product = findProductByEan(products, dto.getEan());
                if (product != null) {
                    Double productPrice = (Double) product.get("current_price");
                    if (productPrice != null) {
                        totalCurrentPrice[0] += productPrice;
                    }
                }

                return ingredient;
            }).collect(Collectors.toList());

            recipe.setIngredients(updatedIngredients);
            recipe.setCurrentPrice(totalCurrentPrice[0]);

            if (recipe.getStoredPrice() == 0 || Math.abs(totalCurrentPrice[0] - recipe.getStoredPrice()) > recipe.getStoredPrice() * 0.05) {
                recipe.setStoredPrice(totalCurrentPrice[0]);
            }

            recipe.setPriceLastUpdated(LocalDateTime.now());
        }

        if (updateRecipeDto.getImageUrl() != null && !updateRecipeDto.getImageUrl().isEmpty()) {
            recipe.setImageUrl(updateRecipeDto.getImageUrl());
        }

        return recipeRepository.save(recipe);
    }


    @Transactional
    public void deleteRecipe(String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = null;

        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            userId = jwtAuth.getTokenAttributes().get("sub").toString(); // Assuming "sub" contains the userId
        }

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));

        if (!recipe.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this recipe");
        }

        recipeRepository.delete(recipe);
    }


    public Page<SavedRecipe> getSavedRecipesForUser(String userId, Pageable pageable) {
        User user = userRepository.findBySupabaseUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return savedRecipeRepository.findByUser(user, pageable);
    }


    public boolean saveRecipeForUser(String userId, String recipeId) {
        Optional<User> userOpt = userRepository.findById(Long.parseLong(userId));
        Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);

        if (userOpt.isPresent() && recipeOpt.isPresent()) {
            User user = userOpt.get();
            Recipe recipe = recipeOpt.get();

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

    @Transactional(rollbackFor = Exception.class)
    public Page<Recipe> searchRecipes(String query, int maxDistance, double minPrice, double maxPrice, String sortBy, String sortDirection, Pageable pageable) {
        Sort sort;

        if ("price".equalsIgnoreCase(sortBy)) {
            sort = "desc".equalsIgnoreCase(sortDirection)
                    ? Sort.by("storedPrice").descending()
                    : Sort.by("storedPrice").ascending();
        } else if ("date".equalsIgnoreCase(sortBy)) {
            sort = "desc".equalsIgnoreCase(sortDirection)
                    ? Sort.by("createdAt").descending()
                    : Sort.by("createdAt").ascending();
        } else {
            throw new IllegalArgumentException("Invalid sortBy parameter. Use 'price' or 'date'.");
        }

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        try {
            return (query == null || query.trim().isEmpty())
                    ? recipeRepository.findAllWithPriceFilter(minPrice, maxPrice, sortedPageable)
                    : recipeRepository.findByNameOrTagsWithPriceFilter(query, minPrice, maxPrice, sortedPageable);
        } catch (Exception e) {
            throw new RuntimeException("Search failed", e);
        }
    }

    private void updateRecipePrice(Recipe recipe, List<Ingredient> ingredients) {
        List<Map<String, Object>> products = productDataService.getProductsByEANsAndStoreCodes(ingredients);
        double currentPrice = calculateCurrentPrice(products);

        recipe.setCurrentPrice(roundToTwoDecimalPlaces(currentPrice));

        if (recipe.getStoredPrice() == 0 || Math.abs(currentPrice - recipe.getStoredPrice()) > recipe.getStoredPrice() * 0.05) {
            recipe.setStoredPrice(roundToTwoDecimalPlaces(currentPrice));
        }

        recipe.setPriceLastUpdated(LocalDateTime.now());
        recipeRepository.save(recipe);
    }

    private double calculateCurrentPrice(List<Map<String, Object>> products) {
        BigDecimal currentPrice = BigDecimal.ZERO;

        for (Map<String, Object> product : products) {
            Double productPrice = (Double) product.get("current_price");
            if (productPrice != null) {
                currentPrice = currentPrice.add(BigDecimal.valueOf(productPrice));
            }
        }

        return currentPrice.doubleValue();
    }

    private double roundToTwoDecimalPlaces(double value) {
        BigDecimal roundedValue = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
        return roundedValue.doubleValue();
    }

    private Map<String, Object> findProductByEan(List<Map<String, Object>> products, String ean) {
        return products.stream()
                .filter(product -> ean.equals(product.get("ean")))
                .findFirst()
                .orElse(null);
    }

}
