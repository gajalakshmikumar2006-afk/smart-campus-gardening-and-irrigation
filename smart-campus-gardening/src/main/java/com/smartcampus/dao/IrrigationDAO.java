package com.smartcampus.dao;

import com.smartcampus.model.IrrigationRecord;
import com.smartcampus.model.IrrigationSchedule;
import com.smartcampus.model.LabelValue;
import com.smartcampus.util.DBUtil;
import com.smartcampus.util.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class IrrigationDAO {
    private static final String SELECT =
        "SELECT s.schedule_id, s.garden_id, g.garden_name, s.scheduled_at, " +
        "COALESCE(r.estimated_water_used_liters, s.estimated_water_liters) AS est_liters, " +
        "s.assigned_user_id, u.full_name AS user_name, s.status, s.notes " +
        "FROM irrigation_schedules s JOIN gardens g ON g.garden_id = s.garden_id " +
        "LEFT JOIN users u ON u.user_id = s.assigned_user_id " +
        "LEFT JOIN irrigation_records r ON r.schedule_id = s.schedule_id ";

    private IrrigationSchedule map(ResultSet rs) throws SQLException {
        IrrigationSchedule s = new IrrigationSchedule();
        s.setScheduleId(rs.getInt("schedule_id"));
        s.setGardenId(rs.getInt("garden_id"));
        s.setGardenName(rs.getString("garden_name"));
        s.setScheduledAt(rs.getTimestamp("scheduled_at"));
        s.setEstimatedWaterLiters(rs.getDouble("est_liters"));
        int uid = rs.getInt("assigned_user_id");
        s.setAssignedUserId(rs.wasNull() ? null : uid);
        s.setAssignedUserName(rs.getString("user_name"));
        s.setStatus(rs.getString("status"));
        s.setNotes(rs.getString("notes"));
        return s;
    }

    /** Pending + rescheduled schedules, soonest first. userId null = all users. */
    public List<IrrigationSchedule> findUpcoming(Integer userId) {
        return list("WHERE s.status IN ('PENDING','RESCHEDULED') " + (userId != null ? "AND s.assigned_user_id = ? " : "")
                + "ORDER BY s.scheduled_at ASC", userId, 0);
    }

    /** Completed + cancelled schedules, newest first. */
    public List<IrrigationSchedule> findHistory(Integer userId, int limit) {
        return list("WHERE s.status IN ('COMPLETED','CANCELLED') " + (userId != null ? "AND s.assigned_user_id = ? " : "")
                + "ORDER BY s.scheduled_at DESC LIMIT ?", userId, limit);
    }

    /** Pending/rescheduled schedules due within the next N hours (for reminders). */
    public List<IrrigationSchedule> findDueSoon(Integer userId, int hours) {
        String sql = SELECT + "WHERE s.status IN ('PENDING','RESCHEDULED') AND s.scheduled_at <= DATE_ADD(NOW(), INTERVAL ? HOUR) " +
                (userId != null ? "AND s.assigned_user_id = ? " : "") + "ORDER BY s.scheduled_at";
        List<IrrigationSchedule> out = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, hours);
            if (userId != null) ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) out.add(map(rs)); }
        } catch (SQLException e) { throw new DataAccessException("findDueSoon failed", e); }
        return out;
    }

    private List<IrrigationSchedule> list(String tail, Integer userId, int limit) {
        List<IrrigationSchedule> out = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(SELECT + tail)) {
            int i = 1;
            if (userId != null) ps.setInt(i++, userId);
            if (limit > 0) ps.setInt(i, limit);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) out.add(map(rs)); }
        } catch (SQLException e) { throw new DataAccessException("Irrigation list failed", e); }
        return out;
    }

    public IrrigationSchedule findById(int id) {
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(SELECT + "WHERE s.schedule_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw new DataAccessException("Irrigation findById failed", e); }
    }

    public int insert(Connection c, IrrigationSchedule s, int createdBy) throws SQLException {
        String sql = "INSERT INTO irrigation_schedules (garden_id, scheduled_at, estimated_water_liters, assigned_user_id, status, notes, created_by) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getGardenId());
            ps.setTimestamp(2, s.getScheduledAt());
            ps.setDouble(3, s.getEstimatedWaterLiters());
            if (s.getAssignedUserId() == null) ps.setNull(4, java.sql.Types.INTEGER); else ps.setInt(4, s.getAssignedUserId());
            ps.setString(5, s.getStatus());
            ps.setString(6, s.getNotes());
            ps.setInt(7, createdBy);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : 0; }
        }
    }

    public void update(Connection c, IrrigationSchedule s) throws SQLException {
        String sql = "UPDATE irrigation_schedules SET garden_id=?, scheduled_at=?, estimated_water_liters=?, assigned_user_id=?, status=?, notes=? WHERE schedule_id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, s.getGardenId());
            ps.setTimestamp(2, s.getScheduledAt());
            ps.setDouble(3, s.getEstimatedWaterLiters());
            if (s.getAssignedUserId() == null) ps.setNull(4, java.sql.Types.INTEGER); else ps.setInt(4, s.getAssignedUserId());
            ps.setString(5, s.getStatus());
            ps.setString(6, s.getNotes());
            ps.setInt(7, s.getScheduleId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(Connection c, int id, String status) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE irrigation_schedules SET status=? WHERE schedule_id=?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void insertRecord(Connection c, IrrigationRecord r, int recordedBy) throws SQLException {
        String sql = "INSERT INTO irrigation_records (schedule_id, completed_at, estimated_water_used_liters, notes, recorded_by) VALUES (?,?,?,?,?) " +
                     "ON DUPLICATE KEY UPDATE completed_at=VALUES(completed_at), estimated_water_used_liters=VALUES(estimated_water_used_liters), notes=VALUES(notes)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, r.getScheduleId());
            ps.setTimestamp(2, r.getCompletedAt());
            ps.setDouble(3, r.getEstimatedWaterUsedLiters());
            ps.setString(4, r.getNotes());
            ps.setInt(5, recordedBy);
            ps.executeUpdate();
        }
    }

    public Timestamp lastCompletedAt(int gardenId) {
        String sql = "SELECT MAX(r.completed_at) FROM irrigation_records r JOIN irrigation_schedules s ON s.schedule_id = r.schedule_id WHERE s.garden_id = ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, gardenId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getTimestamp(1) : null; }
        } catch (SQLException e) { throw new DataAccessException("lastCompletedAt failed", e); }
    }

    /** True if a pending/rescheduled schedule already exists for this garden on the same day. */
    public boolean openScheduleExists(int gardenId, Timestamp when, int excludeId) {
        String sql = "SELECT 1 FROM irrigation_schedules WHERE garden_id=? AND DATE(scheduled_at)=DATE(?) " +
                     "AND status IN ('PENDING','RESCHEDULED') AND schedule_id <> ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, gardenId);
            ps.setTimestamp(2, when);
            ps.setInt(3, excludeId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new DataAccessException("openScheduleExists failed", e); }
    }

    public List<LabelValue> countByStatus() {
        List<LabelValue> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT status, COUNT(*) FROM irrigation_schedules GROUP BY status ORDER BY status");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new LabelValue(rs.getString(1), rs.getInt(2)));
        } catch (SQLException e) { throw new DataAccessException("irrigation countByStatus failed", e); }
        return list;
    }

    public int countUpcoming() {
        return scalar("SELECT COUNT(*) FROM irrigation_schedules WHERE status IN ('PENDING','RESCHEDULED') AND scheduled_at >= CURDATE()");
    }

    public int countCompleted() { return scalar("SELECT COUNT(*) FROM irrigation_schedules WHERE status = 'COMPLETED'"); }

    private int scalar(String sql) {
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { throw new DataAccessException("scalar failed", e); }
    }
}
