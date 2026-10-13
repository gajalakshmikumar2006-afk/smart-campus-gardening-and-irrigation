package com.smartcampus.dao;

import com.smartcampus.model.LabelValue;
import com.smartcampus.model.ReportRow;
import com.smartcampus.util.DBUtil;
import com.smartcampus.util.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Read-only aggregate queries for analytics, dashboard charts and reports. */
public class AnalyticsDAO {

    private static Timestamp start(LocalDate d) { return Timestamp.valueOf(d.atStartOfDay()); }
    private static Timestamp endExclusive(LocalDate d) { return Timestamp.valueOf(d.plusDays(1).atStartOfDay()); }

    /** Total estimated litres and number of irrigation activities in range. gardenId 0 = all gardens. */
    public double[] totals(LocalDate from, LocalDate to, int gardenId) {
        String sql = "SELECT COALESCE(SUM(r.estimated_water_used_liters),0), COUNT(*) FROM irrigation_records r " +
                     "JOIN irrigation_schedules s ON s.schedule_id = r.schedule_id " +
                     "WHERE r.completed_at >= ? AND r.completed_at < ? " + (gardenId > 0 ? "AND s.garden_id = ?" : "");
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, start(from));
            ps.setTimestamp(2, endExclusive(to));
            if (gardenId > 0) ps.setInt(3, gardenId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? new double[]{rs.getDouble(1), rs.getInt(2)} : new double[]{0, 0}; }
        } catch (SQLException e) { throw new DataAccessException("totals failed", e); }
    }

    /** granularity: DAILY, WEEKLY (week starting Monday) or MONTHLY. */
    public List<LabelValue> usageOverTime(String granularity, LocalDate from, LocalDate to, int gardenId) {
        String expr;
        switch (granularity) {
            case "WEEKLY":  expr = "DATE_FORMAT(DATE_SUB(DATE(r.completed_at), INTERVAL WEEKDAY(r.completed_at) DAY), '%Y-%m-%d')"; break;
            case "MONTHLY": expr = "DATE_FORMAT(r.completed_at, '%Y-%m')"; break;
            default:        expr = "DATE_FORMAT(r.completed_at, '%Y-%m-%d')";
        }
        String sql = "SELECT " + expr + " AS label, SUM(r.estimated_water_used_liters) AS total FROM irrigation_records r " +
                     "JOIN irrigation_schedules s ON s.schedule_id = r.schedule_id " +
                     "WHERE r.completed_at >= ? AND r.completed_at < ? " + (gardenId > 0 ? "AND s.garden_id = ? " : "") +
                     "GROUP BY label ORDER BY label";
        return labelValues(sql, from, to, gardenId);
    }

    public List<LabelValue> usageByGarden(LocalDate from, LocalDate to, int gardenId) {
        String sql = "SELECT g.garden_name AS label, SUM(r.estimated_water_used_liters) AS total FROM irrigation_records r " +
                     "JOIN irrigation_schedules s ON s.schedule_id = r.schedule_id JOIN gardens g ON g.garden_id = s.garden_id " +
                     "WHERE r.completed_at >= ? AND r.completed_at < ? " + (gardenId > 0 ? "AND s.garden_id = ? " : "") +
                     "GROUP BY g.garden_id, g.garden_name ORDER BY total DESC";
        return labelValues(sql, from, to, gardenId);
    }

    private List<LabelValue> labelValues(String sql, LocalDate from, LocalDate to, int gardenId) {
        List<LabelValue> out = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, start(from));
            ps.setTimestamp(2, endExclusive(to));
            if (gardenId > 0) ps.setInt(3, gardenId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(new LabelValue(rs.getString("label"), Math.round(rs.getDouble("total") * 10.0) / 10.0));
            }
        } catch (SQLException e) { throw new DataAccessException("usage query failed", e); }
        return out;
    }

    /** Unified report. type: ALL, IRRIGATION or MAINTENANCE. */
    public List<ReportRow> report(LocalDate from, LocalDate to, int gardenId, String type) {
        String irr = "SELECT 'IRRIGATION' AS activity_type, r.completed_at AS activity_date, g.garden_name AS garden_name, " +
                "CONCAT('Irrigation completed - ', r.estimated_water_used_liters, ' L (estimated)') AS details, u.full_name AS staff_name, 'COMPLETED' AS status " +
                "FROM irrigation_records r JOIN irrigation_schedules s ON s.schedule_id = r.schedule_id JOIN gardens g ON g.garden_id = s.garden_id " +
                "LEFT JOIN users u ON u.user_id = r.recorded_by WHERE r.completed_at >= ? AND r.completed_at < ? " + (gardenId > 0 ? "AND s.garden_id = ? " : "");
        String mnt = "SELECT 'MAINTENANCE' AS activity_type, COALESCE(t.completed_at, CAST(t.due_date AS DATETIME)) AS activity_date, g.garden_name AS garden_name, " +
                "CONCAT(t.title, ' (', t.priority, ' priority)') AS details, u.full_name AS staff_name, t.status AS status " +
                "FROM maintenance_tasks t JOIN gardens g ON g.garden_id = t.garden_id LEFT JOIN users u ON u.user_id = t.assigned_user_id " +
                "WHERE COALESCE(t.completed_at, CAST(t.due_date AS DATETIME)) >= ? AND COALESCE(t.completed_at, CAST(t.due_date AS DATETIME)) < ? " +
                (gardenId > 0 ? "AND t.garden_id = ? " : "");
        boolean wantIrr = !"MAINTENANCE".equals(type), wantMnt = !"IRRIGATION".equals(type);
        String sql = wantIrr && wantMnt ? "(" + irr + ") UNION ALL (" + mnt + ") ORDER BY activity_date DESC"
                   : wantIrr ? irr + "ORDER BY activity_date DESC" : mnt + "ORDER BY activity_date DESC";
        List<ReportRow> out = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            for (int part = 0; part < (wantIrr && wantMnt ? 2 : 1); part++) {
                ps.setTimestamp(i++, start(from));
                ps.setTimestamp(i++, endExclusive(to));
                if (gardenId > 0) ps.setInt(i++, gardenId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportRow r = new ReportRow();
                    r.setActivityType(rs.getString("activity_type"));
                    r.setActivityDate(rs.getTimestamp("activity_date"));
                    r.setGardenName(rs.getString("garden_name"));
                    r.setDetails(rs.getString("details"));
                    r.setStaffName(rs.getString("staff_name"));
                    r.setStatus(rs.getString("status"));
                    out.add(r);
                }
            }
        } catch (SQLException e) { throw new DataAccessException("report query failed", e); }
        return out;
    }
}
