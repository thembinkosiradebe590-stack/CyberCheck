package com.fileguard.repository;

import com.fileguard.database.DatabaseManager;
import com.fileguard.model.MonitoredFolder;

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
import java.util.Optional;

/**
 * Stores and retrieves monitored folders.
 */
public class FolderRepository {

    private static final String INSERT_FOLDER = """
            INSERT INTO monitored_folders (
                user_id,
                folder_path,
                active,
                created_at
            )
            VALUES (?, ?, ?, ?)
            """;

    private static final String SELECT_BY_ID = """
            SELECT
                id,
                user_id,
                folder_path,
                active,
                created_at
            FROM monitored_folders
            WHERE id = ?
            """;

    private static final String SELECT_ALL = """
            SELECT
                id,
                user_id,
                folder_path,
                active,
                created_at
            FROM monitored_folders
            ORDER BY id
            """;

    private final DatabaseManager databaseManager;

    public FolderRepository(
            DatabaseManager databaseManager
    ) {
        this.databaseManager = Objects.requireNonNull(
                databaseManager,
                "Database manager cannot be null."
        );
    }

    /**
     * Saves a monitored folder and returns it with its database ID.
     */
    public MonitoredFolder save(
            MonitoredFolder folder
    ) throws SQLException {
        Objects.requireNonNull(
                folder,
                "Monitored folder cannot be null."
        );

        try (Connection connection =
                     databaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             INSERT_FOLDER,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setLong(
                    1,
                    folder.getUserId()
            );

            statement.setString(
                    2,
                    folder.getFolderPath().toString()
            );

            statement.setInt(
                    3,
                    folder.isActive() ? 1 : 0
            );

            statement.setString(
                    4,
                    folder.getCreatedAt().toString()
            );

            statement.executeUpdate();

            try (ResultSet generatedKeys =
                         statement.getGeneratedKeys()) {

                if (!generatedKeys.next()) {
                    throw new SQLException(
                            "The monitored folder ID was not generated."
                    );
                }

                long generatedId =
                        generatedKeys.getLong(1);

                return new MonitoredFolder(
                        generatedId,
                        folder.getUserId(),
                        folder.getFolderPath(),
                        folder.isActive(),
                        folder.getCreatedAt()
                );
            }
        }
    }

    /**
     * Finds one monitored folder by its database ID.
     */
    public Optional<MonitoredFolder> findById(
            long id
    ) throws SQLException {
        try (Connection connection =
                     databaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             SELECT_BY_ID
                     )) {

            statement.setLong(1, id);

            try (ResultSet results =
                         statement.executeQuery()) {

                if (results.next()) {
                    return Optional.of(
                            mapFolder(results)
                    );
                }

                return Optional.empty();
            }
        }
    }

    /**
     * Returns all monitored folders.
     */
    public List<MonitoredFolder> findAll()
            throws SQLException {
        List<MonitoredFolder> folders =
                new ArrayList<>();

        try (Connection connection =
                     databaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             SELECT_ALL
                     );

             ResultSet results =
                     statement.executeQuery()) {

            while (results.next()) {
                folders.add(
                        mapFolder(results)
                );
            }
        }

        return folders;
    }

    private MonitoredFolder mapFolder(
            ResultSet results
    ) throws SQLException {
        return new MonitoredFolder(
                results.getLong("id"),
                results.getLong("user_id"),
                Path.of(
                        results.getString("folder_path")
                ),
                results.getInt("active") == 1,
                Instant.parse(
                        results.getString("created_at")
                )
        );
    }
}