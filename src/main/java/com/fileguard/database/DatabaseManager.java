package com.fileguard.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/**
 * Creates connections to the FileGuard SQLite database.
 */
public class DatabaseManager {

    private final Path databasePath;
    private final String databaseUrl;

    public DatabaseManager(Path databasePath) {
        Objects.requireNonNull(
                databasePath,
                "Database path cannot be null."
        );

        this.databasePath =
                databasePath.toAbsolutePath().normalize();

        createParentDirectory();

        this.databaseUrl =
                "jdbc:sqlite:" + this.databasePath;
    }

    /**
     * Opens a new database connection.
     */
    public Connection getConnection()
            throws SQLException {
        Connection connection =
                DriverManager.getConnection(databaseUrl);

        enableForeignKeys(connection);

        return connection;
    }

    public Path getDatabasePath() {
        return databasePath;
    }

    private void createParentDirectory() {
        Path parentDirectory =
                databasePath.getParent();

        if (parentDirectory == null) {
            return;
        }

        try {
            Files.createDirectories(parentDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "The database directory could not be created.",
                    exception
            );
        }
    }

    private void enableForeignKeys(
            Connection connection
    ) throws SQLException {
        try (Statement statement =
                     connection.createStatement()) {

            statement.execute(
                    "PRAGMA foreign_keys = ON"
            );
        }
    }
}