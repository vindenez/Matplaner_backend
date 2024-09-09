package com.example.tasterj.repository;

import com.example.tasterj.model.Recipe;
import com.example.tasterj.model.SavedRecipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, String> {

    // Find recipes by user Supabase userId
    Page<Recipe> findByUser_SupabaseUserId(String supabaseUserId, Pageable pageable);

    // General find all method
    Page<Recipe> findAll(Pageable pageable);
}


