package com.example.tasterj.repository;

import com.example.tasterj.model.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, String> {

    Page<Recipe> findByUser_SupabaseUserId(String supabaseUserId, Pageable pageable);

    Page<Recipe> findAll(Pageable pageable);
}

