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

    /**
     * Method for voting on a recipe. If a user has already voted, update the existing vote.
     * Otherwise, create a new vote.
     *
     * @param userId   the ID of the user who is voting
     * @param recipeId the ID of the recipe
     * @param upvote   boolean representing whether it's an upvote or downvote (null means removing the vote)
     */
    @Transactional
    public void vote(String userId, String recipeId, Boolean upvote) {
        // Retrieve the recipe by ID
        Recipe recipe = recipeService.getRecipeById(recipeId);
        if (recipe == null) {
            throw new RuntimeException("Recipe not found with ID: " + recipeId);
        }

        // Retrieve the user by their Supabase user ID
        User user = userService.getUserBySupabaseUserId(userId);
        if (user == null) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        // Check if the user has already voted on this recipe
        Optional<Vote> existingVoteOptional = voteRepository.findByUser_SupabaseUserIdAndRecipe_Id(userId, recipeId);
        VoteType voteType = (upvote == null) ? VoteType.NONE : (upvote ? VoteType.UPVOTE : VoteType.DOWNVOTE);

        // Update the existing vote if present, otherwise create a new vote
        if (existingVoteOptional.isPresent()) {
            Vote existingVote = existingVoteOptional.get();
            if (voteType == VoteType.NONE) {
                // If no vote type, remove the existing vote
                voteRepository.delete(existingVote);
            } else {
                // Update the existing vote type
                existingVote.setVoteType(voteType);
                voteRepository.save(existingVote);
            }
        } else if (voteType != VoteType.NONE) {
            // If no existing vote, create a new vote
            Vote newVote = new Vote();
            newVote.setUser(user);
            newVote.setRecipe(recipe);
            newVote.setVoteType(voteType);
            voteRepository.save(newVote);
        }
    }

    /**
     * Method for retrieving a recipe along with its upvote/downvote counts and the user's vote type (if logged in).
     *
     * @param recipeId the ID of the recipe
     * @param userId   the ID of the user to check if they have voted (null if not logged in)
     * @return RecipeWithVotesDto containing the recipe details, upvotes, downvotes, and user vote type
     */
    public RecipeWithVotesDto getRecipeWithVotes(String recipeId, String userId) {
        // Retrieve the recipe
        Recipe recipe = recipeService.getRecipeById(recipeId);
        if (recipe == null) {
            throw new RuntimeException("Recipe not found with ID: " + recipeId);
        }

        // Count upvotes and downvotes for the recipe
        int upvotes = voteRepository.countByRecipeAndVoteType(recipe, VoteType.UPVOTE);
        int downvotes = voteRepository.countByRecipeAndVoteType(recipe, VoteType.DOWNVOTE);

        // Determine the user's vote type (if logged in)
        VoteType userVoteType = VoteType.NONE;
        if (userId != null) {
            Optional<Vote> userVote = voteRepository.findByUser_SupabaseUserIdAndRecipe_Id(userId, recipeId);
            if (userVote.isPresent()) {
                userVoteType = userVote.get().getVoteType();
            }
        }

        // Return the RecipeWithVotesDto with vote data
        return new RecipeWithVotesDto(recipe, upvotes, downvotes, userVoteType);
    }
}
