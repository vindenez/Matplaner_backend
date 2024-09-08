package com.example.tasterj.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "saved_recipes")
public class SavedRecipe {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(name = "saved_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime savedAt;

    public SavedRecipe() {
        this.id = UUID.randomUUID().toString();
    }

    @PrePersist
    protected void onCreate() {
        this.savedAt = LocalDateTime.now();
    }
}