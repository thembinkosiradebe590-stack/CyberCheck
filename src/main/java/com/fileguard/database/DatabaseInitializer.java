package com.fileguard.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/**
 * Creates the FileGuard database tables from schema.sql.
 */
public class DatabaseInitializer {

    private static final String SCHEMA_RESOURCE =
            "database/schema.sql";

    private final DatabaseManager databaseManager;

    public DatabaseInitializer(
            DatabaseManager databaseManager
    ) {
        this.databaseManager = Objects.requireNonNull(
                databaseManager,
                "Database manager cannot be null."
        );
    }

    /**
     * Creates all required tables and indexes.
     */
    public void initialize() throws SQLException {
        String schema = readSchema();

        try (Connection connection =
                     databaseManager.getConnection()) {

            executeStatements(connection, schema);
        }
    }

    private String readSchema() throws SQLException {
        ClassLoader classLoader =
                DatabaseInitializer.class.getClassLoader();

        try (InputStream inputStream =
                     classLoader.getResourceAsStream(
                             SCHEMA_RESOURCE
                     )) {

            if (inputStream == null) {
                throw new SQLException(
                        "Database schema resource was not found."
                );
            }

            return new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );

        } catch (IOException exception) {
            throw new SQLException(
                    "Database schema could not be read.",
                    exception
            );
        }
    }

    private void executeStatements(
            Connection connection,
            String schema
    ) throws SQLException {
        String[] sqlStatements = schema.split(";");

        try (Statement statement =
                     connection.createStatement()) {

            for (String sql : sqlStatements) {
                String trimmedSql = sql.trim();

                if (!trimmedSql.isEmpty()) {
                    statement.execute(trimmedSql);
                }
            }
        }
    }
}