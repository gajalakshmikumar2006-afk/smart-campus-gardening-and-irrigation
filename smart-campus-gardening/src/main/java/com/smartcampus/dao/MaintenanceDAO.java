package com.smartcampus.dao;

import com.smartcampus.model.LabelValue;
import com.smartcampus.model.MaintenanceTask;
import com.smartcampus.util.DBUtil;
import com.smartcampus.util.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceDAO {
    private static final String SELECT =
        "SELECT t.task_id, t.garden_id, g.garden_name, t.assigned_user_id, u.full_name AS user_name, t.title, t.description, " +
        "t.priority, t.due_date, t.status, (t.status <> 'COMPLETED' AND t.due_date < CURDATE()) AS overdue " +
        "FROM maintenance_tasks t JOIN gardens g ON g.garden_id = t.garden_id " +
        "LEFT JOIN users u ON u.user_id = t.assigned_user_id ";

    private MaintenanceTask map(ResultSet rs) throws SQLException {
        MaintenanceTask t = new MaintenanceTask();
        t.setTaskId(rs.getInt("task_id"));
        t.setGardenId(rs.getInt("garden_id"));
        t.setGardenName(rs.getString("garden_name"));
        int uid = rs.getInt("assigned_user_id");
        t.setAssignedUserId(rs.wasNull() ? null : uid);
        t.setAssignedUserName(rs.getString("user_name"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setPriority(rs.getString("priority"));
        t.setDueDate(rs.getDate("due_date"));
        t.setStatus(rs.getString("status"));
        t.setOverdue(rs.getBoolean("overdue"));
        return t;
    }

    /** userId null = all tasks; status null/blank = all statuses. */
    public List<MaintenanceTask> findAll(Integer userId, String status) {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1=1 ");
        if (userId != null) sql.append("AND t.assigned_user_id = ? ");
        boolean hasStatus = status != null && !status.isBlank();
        if (hasStatus) sql.append("AND t.status = ? ");
        sql.append("ORDER BY (t.status = 'COMPLETED'), t.due_date ASC, FIELD(t.priority,'HIGH','MEDIUM','LOW')");
        List<MaintenanceTask> out = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            if (userId != null) ps.setInt(i++, userId);
            if (hasStatus) ps.setString(i, status);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) out.add(map(rs)); }
        } catch (SQLException e) { throw new DataAccessException("Maintenance findAll failed", e); }
        return out;
    }

    public MaintenanceTask findById(int id) {
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(SELECT + "WHERE t.task_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw new DataAccessException("Maintenance findById failed", e); }
    }

    public int insert(Connection c, MaintenanceTask t, int createdBy) throws SQLException {
        String sql = "INSERT INTO maintenance_tasks (garden_id, assigned_user_id, title, description, priority, due_date, status, created_by) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getGardenId());
            if (t.getAssignedUserId() == null) ps.setNull(2, java.sql.Types.INTEGER); else ps.setInt(2, t.getAssignedUserId());
            ps.setString(3, t.getTitle());
            ps.setString(4, t.getDescription());
            ps.setString(5, t.getPriority());
            ps.setDate(6, t.getDueDate());
            ps.setString(7, t.getStatus());
            ps.setInt(8, createdBy);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : 0; }
        }
    }

    public void update(Connection c, MaintenanceTask t) throws SQLException {
        String sql = "UPDATE maintenance_tasks SET garden_id=?, assigned_user_id=?, title=?, description=?, priority=?, due_date=?, status=?, " +
                     "completed_at = CASE WHEN ? = 'COMPLETED' THEN COALESCE(completed_at, NOW()) ELSE NULL END WHERE task_id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, t.getGardenId());
            if (t.getAssignedUserId() == null) ps.setNull(2, java.sql.Types.INTEGER); else ps.setInt(2, t.getAssignedUserId());
            ps.setString(3, t.getTitle());
            ps.setString(4, t.getDescription());
            ps.setString(5, t.getPriority());
            ps.setDate(6, t.getDueDate());
            ps.setString(7, t.getStatus());
            ps.setString(8, t.getStatus());
            ps.setInt(9, t.getTaskId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(Connection c, int id, String status) throws SQLException {
        String sql = "UPDATE maintenance_tasks SET status=?, completed_at = CASE WHEN ? = 'COMPLETED' THEN NOW() ELSE NULL END WHERE task_id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, status);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void delete(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM maintenance_tasks WHERE task_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int countByStatusValue(String status) {
        return scalar("SELECT COUNT(*) FROM maintenance_tasks WHERE status = ?", status);
    }

    public int countOverdue(Integer userId) {
        if (userId == null) return scalar("SELECT COUNT(*) FROM maintenance_tasks WHERE status <> 'COMPLETED' AND due_date < CURDATE()", null);
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM maintenance_tasks WHERE status <> 'COMPLETED' AND due_date < CURDATE() AND assigned_user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new DataAccessException("countOverdue failed", e); }
    }

    public List<LabelValue> countByStatus() {
        List<LabelValue> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT status, COUNT(*) FROM maintenance_tasks GROUP BY status ORDER BY status");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new LabelValue(rs.getString(1), rs.getInt(2)));
        } catch (SQLException e) { throw new DataAccessException("maintenance countByStatus failed", e); }
        return list;
    }

    private int scalar(String sql, String param) {
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new DataAccessException("scalar failed", e); }
    }
}
