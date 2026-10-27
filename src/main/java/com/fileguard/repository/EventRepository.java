package com.fileguard.repository;

import com.fileguard.database.DatabaseManager;
import com.fileguard.model.EventStatus;
import com.fileguard.model.EventType;
import com.fileguard.model.SecurityEvent;
import com.fileguard.model.Severity;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Stores and retrieves security events.
 */
public class EventRepository {

    private static final String INSERT_EVENT = """
            INSERT INTO security_events (
                monitored_folder_id,
                file_path,
                event_type,
                severity,
                description,
                occurred_at,
                status
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SELECT_BY_FOLDER = """
            SELECT
                id,
                monitored_folder_id,
                file_path,
                event_type,
                severity,
                description,
                occurred_at,
                status
            FROM security_events
            WHERE monitored_folder_id = ?
            ORDER BY occurred_at, id
            """;

    private static final String UPDATE_STATUS = """
            UPDATE security_events
            SET status = ?
            WHERE id = ?
            """;

    private final DatabaseManager databaseManager;

    public EventRepository(
            DatabaseManager databaseManager
    ) {
        this.databaseManager = Objects.requireNonNull(
                databaseManager,
                "Database manager cannot be null."
        );
    }

    /**
     * Saves an event and returns it with its database ID.
     */
    public SecurityEvent save(
            SecurityEvent event
    ) throws SQLException {
        Objects.requireNonNull(
                event,
                "Security event cannot be null."
        );

        try (Connection connection =
                     databaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             INSERT_EVENT,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setLong(
                    1,
                    event.getMonitoredFolderId()
            );

            statement.setString(
                    2,
                    event.getFilePath().toString()
            );

            statement.setString(
                    3,
                    event.getEventType().name()
            );

            statement.setString(
                    4,
                    event.getSeverity().name()
            );

            statement.setString(
                    5,
                    event.getDescription()
            );

            statement.setString(
                    6,
                    event.getOccurredAt().toString()
            );

            statement.setString(
                    7,
                    event.getStatus().name()
            );

            statement.executeUpdate();

            try (ResultSet generatedKeys =
                         statement.getGeneratedKeys()) {

                if (!generatedKeys.next()) {
                    throw new SQLException(
                            "The security event ID was not generated."
                    );
                }

                long generatedId =
                        generatedKeys.getLong(1);

                return new SecurityEvent(
                        generatedId,
                        event.getMonitoredFolderId(),
                        event.getFilePath(),
                        event.getEventType(),
                        event.getSeverity(),
                        event.getDescription(),
                        event.getOccurredAt(),
                        event.getStatus()
                );
            }
        }
    }

    /**
     * Returns all events belonging to a monitored folder.
     */
    public List<SecurityEvent> findByFolderId(
            long monitoredFolderId
    ) throws SQLException {
        List<SecurityEvent> events =
                new ArrayList<>();

        try (Connection connection =
                     databaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             SELECT_BY_FOLDER
                     )) {

            statement.setLong(
                    1,
                    monitoredFolderId
            );

            try (ResultSet results =
                         statement.executeQuery()) {

                while (results.next()) {
                    events.add(
                            mapEvent(results)
                    );
                }
            }
        }

        return events;
    }

    /**
     * Updates the review status of a stored event.
     */
    public boolean updateStatus(
            long eventId,
            EventStatus status
    ) throws SQLException {
        Objects.requireNonNull(
                status,
                "Event status cannot be null."
        );

        try (Connection connection =
                     databaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             UPDATE_STATUS
                     )) {

            statement.setString(
                    1,
                    status.name()
            );

            statement.setLong(
                    2,
                    eventId
            );

            return statement.executeUpdate() == 1;
        }
    }

    private SecurityEvent mapEvent(
            ResultSet results
    ) throws SQLException {
        return new SecurityEvent(
                results.getLong("id"),
                results.getLong("monitored_folder_id"),
                Path.of(
                        results.getString("file_path")
                ),
                EventType.valueOf(
                        results.getString("event_type")
                ),
                Severity.valueOf(
                        results.getString("severity")
                ),
                results.getString("description"),
                Instant.parse(
                        results.getString("occurred_at")
                ),
                EventStatus.valueOf(
                        results.getString("status")
                )
        );
    }
}