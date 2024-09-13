package com.example.tasterj.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties("savedRecipes")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipe_id", nullable = false)
    @JsonIgnoreProperties("savedByUsers")
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