package com.example.tasterj.controller;


import com.example.tasterj.model.Favorite;
import com.example.tasterj.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping("/check/{recipeId}")
    public ResponseEntity<Boolean> checkFavorite(@RequestHeader("userId") String userId, @PathVariable String recipeId) {
        boolean isFavorited = favoriteService.checkFavorite(userId, recipeId);
        return ResponseEntity.ok(isFavorited);
    }

    @PostMapping("/{recipeId}")
    public ResponseEntity<Favorite> createFavorite(@RequestHeader("userId") String userId, @PathVariable String recipeId) {
        Favorite favorite = favoriteService.createFavorite(userId, recipeId);
        return new ResponseEntity<>(favorite, HttpStatus.CREATED);
    }

    @DeleteMapping("/{recipeId}")
    public ResponseEntity<Void> deleteFavorite(@RequestHeader("userId") String userId, @PathVariable String recipeId) {
        favoriteService.deleteFavorite(userId, recipeId);
        return ResponseEntity.noContent().build();
    }
}

