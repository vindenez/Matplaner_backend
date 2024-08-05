package com.example.tasterj.repository;

import com.example.tasterj.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, String> {
    Optional<Favorite> findByUserIdAndRecipeId(String userId, String recipeId);
    void deleteByUserIdAndRecipeId(String userId, String recipeId);
}
