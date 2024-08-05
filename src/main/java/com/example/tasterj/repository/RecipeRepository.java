package com.example.tasterj.repository;

import com.example.tasterj.model.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, String> {

    @Query("SELECT r FROM Recipe r JOIN r.favorites f WHERE f.user.id = :userId")
    Page<Recipe> findFavoriteRecipesByUserId(String userId, Pageable pageable);
}
