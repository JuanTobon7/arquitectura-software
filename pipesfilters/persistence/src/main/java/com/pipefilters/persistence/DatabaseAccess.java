package com.pipefilters.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseAccess {

    private static final String URL =
            "jdbc:h2:./data/pipesfilters;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=30000";

    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS processed_files (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                file_name VARCHAR(255) NOT NULL,
                sha256 VARCHAR(64) NOT NULL,
                size_bytes BIGINT NOT NULL,
                filter_origin VARCHAR(50) NOT NULL,
                processed_at TIMESTAMP NOT NULL
            )
            """;

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );

        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_SQL);
        }

        return connection;
    }
}