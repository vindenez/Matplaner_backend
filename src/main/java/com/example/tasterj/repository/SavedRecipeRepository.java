package com.example.tasterj.repository;

import com.example.tasterj.model.Recipe;
import com.example.tasterj.model.SavedRecipe;
import com.example.tasterj.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedRecipeRepository extends JpaRepository<SavedRecipe, String> {

    // Find saved recipes by user
    List<SavedRecipe> findByUser(User user);

    // Find if a user has already saved a specific recipe
    Optional<SavedRecipe> findByUserAndRecipe(User user, Recipe recipe);

    // Delete a saved recipe
    void deleteByUserAndRecipe(User user, Recipe recipe);
}
