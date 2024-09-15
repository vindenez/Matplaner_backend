package com.example.tasterj.repository;

import com.example.tasterj.model.Recipe;
import com.example.tasterj.model.Vote;
import com.example.tasterj.model.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByUser_SupabaseUserIdAndRecipe_Id(String supabaseUserId, String recipeId);
    int countByRecipeAndVoteType(Recipe recipe, VoteType voteType);
}
