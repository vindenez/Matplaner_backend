package com.example.tasterj.repository;

import com.example.tasterj.model.Recipe;
import com.example.tasterj.model.Vote;
import com.example.tasterj.model.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserIdAndRecipeId(String userId, String recipeId);
    int countByRecipeAndVoteType(Recipe recipe, VoteType voteType);  // For counting upvotes and downvotes
}

