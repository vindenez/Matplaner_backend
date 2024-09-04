package com.example.tasterj.dto;

import com.example.tasterj.model.Recipe;
import com.example.tasterj.model.VoteType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeWithVotesDto {
    // Getters and Setters
    private Recipe recipe;
    private int upvotes;
    private int downvotes;
    private VoteType userVote;

    public RecipeWithVotesDto() {
    }

    public RecipeWithVotesDto(Recipe recipe, int upvotes, int downvotes, VoteType userVote) {
        this.recipe = recipe;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.userVote = userVote;
    }

}
