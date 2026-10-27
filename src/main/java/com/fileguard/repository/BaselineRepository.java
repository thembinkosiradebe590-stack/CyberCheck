package com.fileguard.repository;

import com.fileguard.database.DatabaseManager;
import com.fileguard.model.FileBaseline;

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
 * Stores and retrieves trusted file baselines.
 */
public class BaselineRepository {

    private static final String INSERT_BASELINE = """
            INSERT INTO file_baselines (
                monitored_folder_id,
                file_path,
                sha256_hash,
                file_size,
                last_modified_at,
                captured_at
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String SELECT_BY_FOLDER = """
            SELECT
                id,
                monitored_folder_id,
                file_path,
                sha256_hash,
                file_size,
                last_modified_at,
                captured_at
            FROM file_baselines
            WHERE monitored_folder_id = ?
            ORDER BY file_path
            """;

    private static final String DELETE_BY_FOLDER = """
            DELETE FROM file_baselines
            WHERE monitored_folder_id = ?
            """;

    private final DatabaseManager databaseManager;

    public BaselineRepository(
            DatabaseManager databaseManager
    ) {
        this.databaseManager = Objects.requireNonNull(
                databaseManager,
                "Database manager cannot be null."
        );
    }

    /**
     * Saves a baseline and returns it with its database ID.
     */
    public FileBaseline save(
            FileBaseline baseline
    ) throws SQLException {
        Objects.requireNonNull(
                baseline,
                "File baseline cannot be null."
        );

        try (Connection connection =
                     databaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             INSERT_BASELINE,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setLong(
                    1,
                    baseline.getMonitoredFolderId()
            );

            statement.setString(
                    2,
                    baseline.getFilePath().toString()
            );

            statement.setString(
                    3,
                    baseline.getSha256Hash()
            );

            statement.setLong(
                    4,
                    baseline.getFileSize()
            );

            statement.setString(
                    5,
                    baseline.getLastModifiedAt().toString()
            );

            statement.setString(
                    6,
                    baseline.getCapturedAt().toString()
            );

            statement.executeUpdate();

            try (ResultSet generatedKeys =
                         statement.getGeneratedKeys()) {

                if (!generatedKeys.next()) {
                    throw new SQLException(
                            "The baseline ID was not generated."
                    );
                }

                long generatedId =
                        generatedKeys.getLong(1);

                return new FileBaseline(
                        generatedId,
                        baseline.getMonitoredFolderId(),
                        baseline.getFilePath(),
                        baseline.getSha256Hash(),
                        baseline.getFileSize(),
                        baseline.getLastModifiedAt(),
                        baseline.getCapturedAt()
                );
            }
        }
    }

    /**
     * Returns all baselines belonging to a monitored folder.
     */
    public List<FileBaseline> findByFolderId(
            long monitoredFolderId
    ) throws SQLException {
        List<FileBaseline> baselines =
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
                    baselines.add(
                            mapBaseline(results)
                    );
                }
            }
        }

        return baselines;
    }

    /**
     * Removes all baselines for a monitored folder.
     */
    public int deleteByFolderId(
            long monitoredFolderId
    ) throws SQLException {
        try (Connection connection =
                     databaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             DELETE_BY_FOLDER
                     )) {

            statement.setLong(
                    1,
                    monitoredFolderId
            );

            return statement.executeUpdate();
        }
    }

    private FileBaseline mapBaseline(
            ResultSet results
    ) throws SQLException {
        return new FileBaseline(
                results.getLong("id"),
                results.getLong("monitored_folder_id"),
                Path.of(
                        results.getString("file_path")
                ),
                results.getString("sha256_hash"),
                results.getLong("file_size"),
                Instant.parse(
                        results.getString(
                                "last_modified_at"
                        )
                ),
                Instant.parse(
                        results.getString("captured_at")
                )
        );
    }
}