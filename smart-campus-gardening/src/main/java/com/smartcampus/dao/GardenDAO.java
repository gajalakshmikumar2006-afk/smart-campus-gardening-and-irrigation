package com.smartcampus.dao;

import com.smartcampus.model.Garden;
import com.smartcampus.model.LabelValue;
import com.smartcampus.util.DBUtil;
import com.smartcampus.util.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GardenDAO {
    private static final String SELECT =
        "SELECT g.garden_id, g.garden_name, g.location, g.area_sqm, g.garden_type, g.description, g.status, " +
        "(SELECT COALESCE(SUM(p.quantity),0) FROM plants p WHERE p.garden_id = g.garden_id) AS plant_count FROM gardens g ";

    private Garden map(ResultSet rs) throws SQLException {
        Garden g = new Garden();
        g.setGardenId(rs.getInt("garden_id"));
        g.setGardenName(rs.getString("garden_name"));
        g.setLocation(rs.getString("location"));
        g.setAreaSqm(rs.getDouble("area_sqm"));
        g.setGardenType(rs.getString("garden_type"));
        g.setDescription(rs.getString("description"));
        g.setStatus(rs.getString("status"));
        g.setPlantCount(rs.getInt("plant_count"));
        return g;
    }

    public List<Garden> findAll() {
        List<Garden> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(SELECT + "ORDER BY g.garden_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { throw new DataAccessException("Garden findAll failed", e); }
        return list;
    }

    public Garden findById(int id) {
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(SELECT + "WHERE g.garden_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw new DataAccessException("Garden findById failed", e); }
    }

    public int insert(Connection c, Garden g) throws SQLException {
        String sql = "INSERT INTO gardens (garden_name, location, area_sqm, garden_type, description, status) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, g);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : 0; }
        }
    }

    public void update(Connection c, Garden g) throws SQLException {
        String sql = "UPDATE gardens SET garden_name=?, location=?, area_sqm=?, garden_type=?, description=?, status=? WHERE garden_id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, g);
            ps.setInt(7, g.getGardenId());
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Garden g) throws SQLException {
        ps.setString(1, g.getGardenName());
        ps.setString(2, g.getLocation());
        ps.setDouble(3, g.getAreaSqm());
        ps.setString(4, g.getGardenType());
        ps.setString(5, g.getDescription());
        ps.setString(6, g.getStatus());
    }

    public void delete(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM gardens WHERE garden_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int count() { return scalar("SELECT COUNT(*) FROM gardens"); }

    public List<LabelValue> countByStatus() {
        List<LabelValue> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT status, COUNT(*) FROM gardens GROUP BY status ORDER BY status");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new LabelValue(rs.getString(1), rs.getInt(2)));
        } catch (SQLException e) { throw new DataAccessException("countByStatus failed", e); }
        return list;
    }

    private int scalar(String sql) {
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { throw new DataAccessException("scalar failed", e); }
    }
}
