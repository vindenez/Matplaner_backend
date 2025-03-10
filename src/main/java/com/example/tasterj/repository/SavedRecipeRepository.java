package com.example.tasterj.repository;

import com.example.tasterj.model.Recipe;
import com.example.tasterj.model.SavedRecipe;
import com.example.tasterj.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedRecipeRepository extends JpaRepository<SavedRecipe, String> {

    Optional<SavedRecipe> findByUserAndRecipe(User user, Recipe recipe);

    @Query("SELECT sr FROM SavedRecipe sr JOIN FETCH sr.recipe WHERE sr.user = :user")
    Page<SavedRecipe> findByUserWithRecipes(@Param("user") User user, Pageable pageable);

    void deleteByUserAndRecipe(User user, Recipe recipe);
}

