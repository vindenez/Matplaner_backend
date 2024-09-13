package com.example.tasterj.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(nullable = false, unique = true)
    private String supabaseUserId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-savedRecipe")
    private List<SavedRecipe> savedRecipes;

}