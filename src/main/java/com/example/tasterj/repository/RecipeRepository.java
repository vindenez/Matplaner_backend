package com.example.tasterj.repository;

import com.example.tasterj.model.Recipe;
import com.example.tasterj.model.SavedRecipe;
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

    Page<Recipe> findByNameContainingIgnoreCaseOrTagsContainingIgnoreCase(String name, String tag, Pageable pageable);
    Page<Recipe> findAll(Pageable pageable);

    @Query(value = "SELECT * FROM recipes WHERE (levenshtein(name, :query) <= :maxDistance OR EXISTS (SELECT 1 FROM unnest(tags) tag WHERE levenshtein(tag, :query) <= :maxDistance)) AND current_price BETWEEN :minPrice AND :maxPrice ORDER BY current_price :sortDirection",
            nativeQuery = true)
    List<Recipe> findByNameOrTagsFuzzyWithPriceFilter(@Param("query") String query, @Param("maxDistance") int maxDistance, @Param("minPrice") double minPrice, @Param("maxPrice") double maxPrice, @Param("sortDirection") String sortDirection, Pageable pageable);

}


