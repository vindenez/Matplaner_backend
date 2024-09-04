package com.example.tasterj.controller;

import com.example.tasterj.dto.RecipeWithVotesDto;
import com.example.tasterj.model.User;
import com.example.tasterj.service.UserService;
import com.example.tasterj.service.VoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/{recipeId}")
    public ResponseEntity<?> voteRecipe(
            @PathVariable String recipeId,
            @RequestBody String userId,  // Extract userId from the request body or DTO
            @RequestParam(required = false) Boolean upvote) {

        User user = userService.getUserBySupabaseUserId(userId);

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

    @GetMapping("/{recipeId}")
    public ResponseEntity<?> getRecipeWithVotes(
            @PathVariable String recipeId,
            @RequestBody String userId) {  // Extract userId from the request body or DTO

        User user = userService.getUserBySupabaseUserId(userId);

        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        try {
            RecipeWithVotesDto recipeWithVotes = voteService.getRecipeWithVotes(recipeId, user.getSupabaseUserId());
            return new ResponseEntity<>(recipeWithVotes, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while fetching recipe votes: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
