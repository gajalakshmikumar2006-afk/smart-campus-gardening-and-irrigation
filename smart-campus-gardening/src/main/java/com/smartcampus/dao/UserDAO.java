package com.smartcampus.dao;

import com.smartcampus.model.User;
import com.smartcampus.util.DBUtil;
import com.smartcampus.util.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final String COLS = "user_id, full_name, email, password_hash, role, status, phone, created_at";

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setStatus(rs.getString("status"));
        u.setPhone(rs.getString("phone"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }

    public User findByEmail(String email) {
        String sql = "SELECT " + COLS + " FROM users WHERE email = ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw new DataAccessException("findByEmail failed", e); }
    }

    public User findById(int id) {
        String sql = "SELECT " + COLS + " FROM users WHERE user_id = ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw new DataAccessException("findById failed", e); }
    }

    public List<User> findAll() {
        return query("SELECT " + COLS + " FROM users ORDER BY role, full_name");
    }

    /** Active maintenance staff - used for assignment drop-downs. */
    public List<User> findActiveStaff() {
        return query("SELECT " + COLS + " FROM users WHERE role = 'STAFF' AND status = 'ACTIVE' ORDER BY full_name");
    }

    public List<Integer> findActiveAdminIds() {
        List<Integer> ids = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT user_id FROM users WHERE role='ADMIN' AND status='ACTIVE'");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ids.add(rs.getInt(1));
        } catch (SQLException e) { throw new DataAccessException("findActiveAdminIds failed", e); }
        return ids;
    }

    private List<User> query(String sql) {
        List<User> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { throw new DataAccessException("user query failed", e); }
        return list;
    }

    public boolean emailExists(String email, int excludeUserId) {
        String sql = "SELECT 1 FROM users WHERE email = ? AND user_id <> ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, excludeUserId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new DataAccessException("emailExists failed", e); }
    }

    public int insert(Connection c, User u) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, password_hash, role, status, phone) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getRole());
            ps.setString(5, u.getStatus());
            ps.setString(6, u.getPhone());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : 0; }
        }
    }

    public void update(Connection c, User u) throws SQLException {
        String sql = "UPDATE users SET full_name=?, email=?, role=?, status=?, phone=? WHERE user_id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getRole());
            ps.setString(4, u.getStatus());
            ps.setString(5, u.getPhone());
            ps.setInt(6, u.getUserId());
            ps.executeUpdate();
        }
    }

    public void updateProfile(Connection c, int id, String name, String phone) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE users SET full_name=?, phone=? WHERE user_id=?")) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void updatePassword(Connection c, int id, String hash) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE users SET password_hash=? WHERE user_id=?")) {
            ps.setString(1, hash);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void setStatus(Connection c, int id, String status) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE users SET status=? WHERE user_id=?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public int countActiveAdmins() {
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE role='ADMIN' AND status='ACTIVE'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { throw new DataAccessException("countActiveAdmins failed", e); }
    }
}
