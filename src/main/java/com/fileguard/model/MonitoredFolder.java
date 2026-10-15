package com.fileguard.model;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

/** Represents a folder selected for file-integrity monitoring. */
public class MonitoredFolder {

    private final long id;
    private final long userId;
    private final Path folderPath;
    private final Instant createdAt;
    private boolean active;

    public MonitoredFolder(
            long id,
            long userId,
            Path folderPath,
            boolean active,
            Instant createdAt
    ) {
        if (id < 0) {
            throw new IllegalArgumentException("The folder ID cannot be negative.");
        }
        if (userId < 0) {
            throw new IllegalArgumentException("The user ID cannot be negative.");
        }

        this.id = id;
        this.userId = userId;
        this.folderPath = Objects.requireNonNull(
                folderPath,
                "Folder path cannot be null."
        ).toAbsolutePath().normalize();
        this.active = active;
        this.createdAt = Objects.requireNonNull(
                createdAt,
                "Creation time cannot be null."
        );
    }

    public MonitoredFolder(long userId, Path folderPath) {
        this(0, userId, folderPath, true, Instant.now());
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public Path getFolderPath() {
        return folderPath;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }
}
