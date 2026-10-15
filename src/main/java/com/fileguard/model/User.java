package com.fileguard.model;

import java.time.Instant;
import java.util.Objects;

/** Represents a FileGuard user. */
public class User {

    private final long id;
    private final String username;
    private final String passwordHash;
    private final Instant createdAt;

    public User(long id, String username, String passwordHash, Instant createdAt) {
        if (id < 0) {
            throw new IllegalArgumentException("The user ID cannot be negative.");
        }

        this.id = id;
        this.username = requireText(username, "Username");
        this.passwordHash = requireText(passwordHash, "Password hash");
        this.createdAt = Objects.requireNonNull(
                createdAt,
                "Creation time cannot be null."
        );
    }

    public User(String username, String passwordHash) {
        this(0, username, passwordHash, Instant.now());
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }

        return value.trim();
    }
}
