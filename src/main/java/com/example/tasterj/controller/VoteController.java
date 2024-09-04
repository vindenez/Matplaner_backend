package com.example.tasterj.controller;

import com.example.tasterj.dto.RecipeWithVotesDto;
import com.example.tasterj.model.User;
import com.example.tasterj.service.UserService;
import com.example.tasterj.service.VoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteService voteService;
    private final UserService userService;

    public VoteController(VoteService voteService, UserService userService) {
        this.voteService = voteService;
        this.userService = userService;
    }

    /**
     * Post a vote for a specific recipe by the logged-in user.
     * @param recipeId - The ID of the recipe to vote for.
     * @param upvote - Boolean for upvote (true) or downvote (false). Can be null to remove a vote.
     * @return Response indicating success or failure of the vote.
     */
    @PostMapping("/{recipeId}")
    public ResponseEntity<String> voteRecipe(
            @PathVariable String recipeId,
            @RequestParam(required = false) Boolean upvote) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String supabaseUserId = authentication.getName();

        User user = userService.getUserBySupabaseUserId(supabaseUserId);

        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        try {
            voteService.vote(user.getSupabaseUserId(), recipeId, upvote);
            return new ResponseEntity<>("Vote successfully recorded", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while processing the vote: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get the recipe and associated votes (upvotes/downvotes) along with the user's vote status if logged in.
     * @param recipeId - The ID of the recipe to fetch.
     * @return RecipeWithVotesDto containing recipe details and vote information.
     */
    @GetMapping("/{recipeId}")
    public ResponseEntity<?> getRecipeWithVotes(@PathVariable String recipeId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String supabaseUserId = null;
            if (authentication != null && authentication.isAuthenticated()) {
                supabaseUserId = authentication.getName();
            }

            RecipeWithVotesDto recipeWithVotes = voteService.getRecipeWithVotes(recipeId, supabaseUserId);
            return new ResponseEntity<>(recipeWithVotes, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while fetching recipe votes: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
