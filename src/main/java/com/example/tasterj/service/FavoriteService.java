package com.example.tasterj.service;

import com.example.tasterj.model.Favorite;
import com.example.tasterj.repository.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Optional;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    public boolean checkFavorite(String userId, String recipeId) {
        return favoriteRepository.findByUserIdAndRecipeId(userId, recipeId).isPresent();
    }

    public Favorite createFavorite(String userId, String recipeId) {
        if (checkFavorite(userId, recipeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Favorite already exists");
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setRecipeId(recipeId);
        return favoriteRepository.save(favorite);
    }

    public void deleteFavorite(String userId, String recipeId) {
        if (!checkFavorite(userId, recipeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite not found");
        }
        favoriteRepository.deleteByUserIdAndRecipeId(userId, recipeId);
    }
}

