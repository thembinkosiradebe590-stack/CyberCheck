package com.fileguard.model;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/** Stores the trusted state of a file at a specific point in time. */
public class FileBaseline {

    private final long id;
    private final long monitoredFolderId;
    private final Path filePath;
    private final String sha256Hash;
    private final long fileSize;
    private final Instant lastModifiedAt;
    private final Instant capturedAt;

    public FileBaseline(
            long id,
            long monitoredFolderId,
            Path filePath,
            String sha256Hash,
            long fileSize,
            Instant lastModifiedAt,
            Instant capturedAt
    ) {
        if (id < 0) {
            throw new IllegalArgumentException("The baseline ID cannot be negative.");
        }
        if (monitoredFolderId < 0) {
            throw new IllegalArgumentException("The monitored folder ID cannot be negative.");
        }
        if (fileSize < 0) {
            throw new IllegalArgumentException("File size cannot be negative.");
        }

        this.id = id;
        this.monitoredFolderId = monitoredFolderId;
        this.filePath = Objects.requireNonNull(
                filePath,
                "File path cannot be null."
        ).toAbsolutePath().normalize();
        this.sha256Hash = validateHash(sha256Hash);
        this.fileSize = fileSize;
        this.lastModifiedAt = Objects.requireNonNull(
                lastModifiedAt,
                "Last-modified time cannot be null."
        );
        this.capturedAt = Objects.requireNonNull(
                capturedAt,
                "Capture time cannot be null."
        );
    }

    public FileBaseline(
            long monitoredFolderId,
            Path filePath,
            String sha256Hash,
            long fileSize,
            Instant lastModifiedAt
    ) {
        this(
                0,
                monitoredFolderId,
                filePath,
                sha256Hash,
                fileSize,
                lastModifiedAt,
                Instant.now()
        );
    }

    public long getId() {
        return id;
    }

    public long getMonitoredFolderId() {
        return monitoredFolderId;
    }

    public Path getFilePath() {
        return filePath;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    public long getFileSize() {
        return fileSize;
    }

    public Instant getLastModifiedAt() {
        return lastModifiedAt;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public boolean matchesHash(String currentHash) {
        return currentHash != null
                && sha256Hash.equals(currentHash.trim().toLowerCase(Locale.ROOT));
    }

    private static String validateHash(String hash) {
        if (hash == null || !hash.matches("(?i)[0-9a-f]{64}")) {
            throw new IllegalArgumentException(
                    "SHA-256 hash must contain exactly 64 hexadecimal characters."
            );
        }

        return hash.toLowerCase(Locale.ROOT);
    }
}
