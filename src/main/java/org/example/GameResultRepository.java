package org.example;

import java.sql.*;

public class GameResultRepository {

    private final Connection conn;

    public GameResultRepository() {
        try {
            this.conn = DatabaseInitializer.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Connection to database failed!", e);
        }
    }

    public int ensurePlayer(String name) throws SQLException {
        String query = "INSERT INTO players(name) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public int insertGame(String type) throws SQLException {
        String query = "INSERT INTO games(type) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, type);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public void insertResult(int gameId, int playerId, int score, boolean winner) throws SQLException {
        String query = "INSERT INTO game_results(game_id, player_id, score, winner) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, gameId);
            ps.setInt(2, playerId);
            ps.setInt(3, score);
            ps.setBoolean(4, winner);
            ps.executeUpdate();
        }
    }
}