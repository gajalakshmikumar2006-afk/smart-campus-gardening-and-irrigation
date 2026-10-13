package com.smartcampus.dao;

import com.smartcampus.model.ActivityLog;
import com.smartcampus.util.DBUtil;
import com.smartcampus.util.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDAO {
    public void insert(Connection c, Integer userId, String type, String description) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO activity_logs (user_id, activity_type, description) VALUES (?,?,?)")) {
            if (userId == null) ps.setNull(1, java.sql.Types.INTEGER); else ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setString(3, description.length() > 300 ? description.substring(0, 300) : description);
            ps.executeUpdate();
        }
    }

    public List<ActivityLog> findRecent(int limit) {
        String sql = "SELECT l.log_id, l.user_id, u.full_name, l.activity_type, l.description, l.created_at " +
                     "FROM activity_logs l LEFT JOIN users u ON u.user_id = l.user_id ORDER BY l.created_at DESC, l.log_id DESC LIMIT ?";
        List<ActivityLog> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ActivityLog a = new ActivityLog();
                    a.setLogId(rs.getInt(1));
                    int uid = rs.getInt(2);
                    a.setUserId(rs.wasNull() ? null : uid);
                    a.setUserName(rs.getString(3));
                    a.setActivityType(rs.getString(4));
                    a.setDescription(rs.getString(5));
                    a.setCreatedAt(rs.getTimestamp(6));
                    list.add(a);
                }
            }
        } catch (SQLException e) { throw new DataAccessException("Activity findRecent failed", e); }
        return list;
    }
}
