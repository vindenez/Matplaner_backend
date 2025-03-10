package com.example.tasterj.service;

import com.example.tasterj.dto.RecipeWithVotesDto;
import com.example.tasterj.model.Recipe;
import com.example.tasterj.model.User;
import com.example.tasterj.model.Vote;
import com.example.tasterj.model.VoteType;
import com.example.tasterj.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final RecipeService recipeService;
    private final UserService userService;

    public VoteService(VoteRepository voteRepository, RecipeService recipeService, UserService userService) {
        this.voteRepository = voteRepository;
        this.recipeService = recipeService;
        this.userService = userService;
    }

    @Transactional
    public void vote(String userId, String recipeId, Boolean upvote) {
        Recipe recipe = recipeService.getRecipeById(recipeId);
        if (recipe == null) {
            throw new RuntimeException("Recipe not found with ID: " + recipeId);
        }

        User user = userService.getUserBySupabaseUserId(userId);
        if (user == null) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        Optional<Vote> existingVoteOptional = voteRepository.findByUser_SupabaseUserIdAndRecipe_Id(userId, recipeId);
        VoteType voteType = (upvote == null) ? VoteType.NONE : (upvote ? VoteType.UPVOTE : VoteType.DOWNVOTE);

        if (existingVoteOptional.isPresent()) {
            Vote existingVote = existingVoteOptional.get();
            if (voteType == VoteType.NONE) {
                voteRepository.delete(existingVote);
            } else {
                existingVote.setVoteType(voteType);
                voteRepository.save(existingVote);
            }
        } else if (voteType != VoteType.NONE) {
            Vote newVote = new Vote();
            newVote.setUser(user);
            newVote.setRecipe(recipe);
            newVote.setVoteType(voteType);
            voteRepository.save(newVote);
        }
    }

    public RecipeWithVotesDto getRecipeWithVotes(String recipeId, String userId) {
        Recipe recipe = recipeService.getRecipeById(recipeId);
        if (recipe == null) {
            throw new RuntimeException("Recipe not found with ID: " + recipeId);
        }

        int upvotes = voteRepository.countByRecipeAndVoteType(recipe, VoteType.UPVOTE);
        int downvotes = voteRepository.countByRecipeAndVoteType(recipe, VoteType.DOWNVOTE);

        VoteType userVoteType = VoteType.NONE;
        if (userId != null) {
            Optional<Vote> userVote = voteRepository.findByUser_SupabaseUserIdAndRecipe_Id(userId, recipeId);
            if (userVote.isPresent()) {
                userVoteType = userVote.get().getVoteType();
            }
        }

        return new RecipeWithVotesDto(recipe, upvotes, downvotes, userVoteType);
    }
}
