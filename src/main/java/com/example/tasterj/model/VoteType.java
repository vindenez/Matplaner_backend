package com.example.tasterj.model;

public enum VoteType {
    UPVOTE(1),
    DOWNVOTE(-1),
    NONE(0); // Represents no vote

    private final int value;

    VoteType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
