package com.smartcampus.dao;

import com.smartcampus.util.DBUtil;
import com.smartcampus.util.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsDAO {
    public String get(String key, String defaultValue) {
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT setting_value FROM app_settings WHERE setting_key = ?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getString(1) : defaultValue; }
        } catch (SQLException e) { throw new DataAccessException("Settings get failed", e); }
    }

    public void set(Connection c, String key, String value) throws SQLException {
        String sql = "INSERT INTO app_settings (setting_key, setting_value) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }
}
