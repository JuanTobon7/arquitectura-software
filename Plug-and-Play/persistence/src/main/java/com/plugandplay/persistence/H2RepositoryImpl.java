package com.plugandplay.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class H2RepositoryImpl implements Repository {

    private final DatabaseAccess databaseAccess;

    public H2RepositoryImpl(DatabaseAccess databaseAccess) {
        this.databaseAccess = databaseAccess;
    }

    @Override
    public synchronized void save(ProcessedFile file) {

        String sql = """
                INSERT INTO processed_files
                (file_name, sha256, ubication, size_bytes, filter_origin, processed_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = databaseAccess.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, file.fileName());
            statement.setString(2, file.sha256());
            statement.setString(3, file.ubication());
            statement.setLong(4, file.sizeBytes());
            statement.setString(5, file.filterOrigin());
            statement.setTimestamp(
                    6,
                    java.sql.Timestamp.valueOf(file.processedAt())
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error persisting processed file",
                    e
            );
        }
    }
}