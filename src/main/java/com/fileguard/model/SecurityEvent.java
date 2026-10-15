package com.fileguard.model;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

/** Represents a file change detected by FileGuard. */
public class SecurityEvent {

    private final long id;
    private final long monitoredFolderId;
    private final Path filePath;
    private final EventType eventType;
    private final Severity severity;
    private final String description;
    private final Instant occurredAt;
    private EventStatus status;

    public SecurityEvent(
            long id,
            long monitoredFolderId,
            Path filePath,
            EventType eventType,
            Severity severity,
            String description,
            Instant occurredAt,
            EventStatus status
    ) {
        if (id < 0) {
            throw new IllegalArgumentException("The event ID cannot be negative.");
        }
        if (monitoredFolderId < 0) {
            throw new IllegalArgumentException("The monitored folder ID cannot be negative.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be blank.");
        }

        this.id = id;
        this.monitoredFolderId = monitoredFolderId;
        this.filePath = Objects.requireNonNull(filePath, "File path cannot be null.")
                .toAbsolutePath()
                .normalize();
        this.eventType = Objects.requireNonNull(eventType, "Event type cannot be null.");
        this.severity = Objects.requireNonNull(severity, "Severity cannot be null.");
        this.description = description.trim();
        this.occurredAt = Objects.requireNonNull(
                occurredAt,
                "Occurrence time cannot be null."
        );
        this.status = Objects.requireNonNull(status, "Event status cannot be null.");
    }

    public SecurityEvent(
            long monitoredFolderId,
            Path filePath,
            EventType eventType,
            Severity severity,
            String description
    ) {
        this(
                0,
                monitoredFolderId,
                filePath,
                eventType,
                severity,
                description,
                Instant.now(),
                EventStatus.OPEN
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

    public EventType getEventType() {
        return eventType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void acknowledge() {
        if (status == EventStatus.RESOLVED) {
            throw new IllegalStateException("A resolved event cannot be acknowledged.");
        }

        status = EventStatus.ACKNOWLEDGED;
    }

    public void resolve() {
        status = EventStatus.RESOLVED;
    }
}
