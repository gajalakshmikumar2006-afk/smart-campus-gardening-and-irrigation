package com.smartcampus.dao;

import com.smartcampus.model.Notification;
import com.smartcampus.util.DBUtil;
import com.smartcampus.util.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
    public void insert(Connection c, int userId, String message, String type) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO notifications (user_id, message, notification_type) VALUES (?,?,?)")) {
            ps.setInt(1, userId);
            ps.setString(2, message.length() > 300 ? message.substring(0, 300) : message);
            ps.setString(3, type);
            ps.executeUpdate();
        }
    }

    public List<Notification> findByUser(int userId, int limit) {
        String sql = "SELECT notification_id, user_id, message, notification_type, is_read, created_at FROM notifications " +
                     "WHERE user_id = ? ORDER BY is_read ASC, created_at DESC LIMIT ?";
        List<Notification> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setNotificationId(rs.getInt(1));
                    n.setUserId(rs.getInt(2));
                    n.setMessage(rs.getString(3));
                    n.setNotificationType(rs.getString(4));
                    n.setRead(rs.getBoolean(5));
                    n.setCreatedAt(rs.getTimestamp(6));
                    list.add(n);
                }
            }
        } catch (SQLException e) { throw new DataAccessException("Notification findByUser failed", e); }
        return list;
    }

    public int countUnread(int userId) {
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new DataAccessException("countUnread failed", e); }
    }

    /** Only marks the notification if it belongs to the given user. */
    public void markRead(int notificationId, int userId) {
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(
                "UPDATE notifications SET is_read = 1 WHERE notification_id = ? AND user_id = ?")) {
            ps.setInt(1, notificationId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new DataAccessException("markRead failed", e); }
    }

    public void markAllRead(int userId) {
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(
                "UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new DataAccessException("markAllRead failed", e); }
    }
}
