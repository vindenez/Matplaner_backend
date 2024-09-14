package com.example.tasterj.repository;

import com.example.tasterj.model.Recipe;
import com.example.tasterj.model.SavedRecipe;
import com.example.tasterj.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, String>, JpaSpecificationExecutor<Recipe> {

    Page<Recipe> findByUser_SupabaseUserId(String supabaseUserId, Pageable pageable);

    Page<Recipe> findAll(Pageable pageable);

    Page<Recipe> findByNameContainingIgnoreCaseOrTagsContainingIgnoreCase(String name, String tag, Pageable pageable);

    @Query("SELECT r FROM Recipe r JOIN r.tags t WHERE (LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t) LIKE LOWER(CONCAT('%', :query, '%'))) AND r.storedPrice BETWEEN :minPrice AND :maxPrice")
    Page<Recipe> findByNameOrTagsWithPriceFilter(@Param("query") String query, @Param("minPrice") double minPrice, @Param("maxPrice") double maxPrice, Pageable pageable);

    @Query("SELECT r FROM Recipe r WHERE r.storedPrice BETWEEN :minPrice AND :maxPrice")
    Page<Recipe> findAllWithPriceFilter(@Param("minPrice") double minPrice, @Param("maxPrice") double maxPrice, Pageable pageable);


    @Query("SELECT r FROM Recipe r JOIN r.tags t WHERE r.userId = :userId AND r.isPublic = false AND (LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t) LIKE LOWER(CONCAT('%', :query, '%'))) AND r.storedPrice BETWEEN :minPrice AND :maxPrice")
    Page<Recipe> findUserPrivateRecipes(@Param("query") String query, @Param("minPrice") double minPrice, @Param("maxPrice") double maxPrice, @Param("userId") String userId, Pageable pageable);

    @Query("SELECT r FROM Recipe r WHERE r.isPublic = true AND r.storedPrice BETWEEN :minPrice AND :maxPrice")
    Page<Recipe> findPublicRecipesWithPriceFilter(@Param("minPrice") double minPrice, @Param("maxPrice") double maxPrice, Pageable pageable);

    @Query("SELECT r FROM Recipe r JOIN r.tags t WHERE (LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t) LIKE LOWER(CONCAT('%', :query, '%'))) AND r.isPublic = true AND r.storedPrice BETWEEN :minPrice AND :maxPrice")
    Page<Recipe> findPublicByNameOrTagsWithPriceFilter(@Param("query") String query, @Param("minPrice") double minPrice, @Param("maxPrice") double maxPrice, Pageable pageable);}


