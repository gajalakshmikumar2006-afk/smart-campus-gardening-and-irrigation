package com.smartcampus.dao;

import com.smartcampus.model.Plant;
import com.smartcampus.util.DBUtil;
import com.smartcampus.util.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PlantDAO {
    private static final String SELECT =
        "SELECT p.plant_id, p.garden_id, g.garden_name, p.plant_name, p.category, p.quantity, " +
        "p.watering_frequency_days, p.water_requirement_liters, p.description " +
        "FROM plants p JOIN gardens g ON g.garden_id = p.garden_id ";

    private Plant map(ResultSet rs) throws SQLException {
        Plant p = new Plant();
        p.setPlantId(rs.getInt("plant_id"));
        p.setGardenId(rs.getInt("garden_id"));
        p.setGardenName(rs.getString("garden_name"));
        p.setPlantName(rs.getString("plant_name"));
        p.setCategory(rs.getString("category"));
        p.setQuantity(rs.getInt("quantity"));
        p.setWateringFrequencyDays(rs.getInt("watering_frequency_days"));
        p.setWaterRequirementLiters(rs.getDouble("water_requirement_liters"));
        p.setDescription(rs.getString("description"));
        return p;
    }

    /** gardenId = 0 and category = null/blank mean "no filter". */
    public List<Plant> findAll(int gardenId, String category) {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (gardenId > 0) { sql.append("AND p.garden_id = ? "); params.add(gardenId); }
        if (category != null && !category.isBlank()) { sql.append("AND p.category = ? "); params.add(category); }
        sql.append("ORDER BY g.garden_name, p.plant_name");
        List<Plant> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { throw new DataAccessException("Plant findAll failed", e); }
        return list;
    }

    public List<Plant> findByGarden(int gardenId) { return findAll(gardenId, null); }

    public Plant findById(int id) {
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(SELECT + "WHERE p.plant_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw new DataAccessException("Plant findById failed", e); }
    }

    public List<String> findCategories() {
        List<String> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT DISTINCT category FROM plants ORDER BY category"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(1));
        } catch (SQLException e) { throw new DataAccessException("findCategories failed", e); }
        return list;
    }

    public int insert(Connection c, Plant p) throws SQLException {
        String sql = "INSERT INTO plants (garden_id, plant_name, category, quantity, watering_frequency_days, water_requirement_liters, description) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, p);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : 0; }
        }
    }

    public void update(Connection c, Plant p) throws SQLException {
        String sql = "UPDATE plants SET garden_id=?, plant_name=?, category=?, quantity=?, watering_frequency_days=?, water_requirement_liters=?, description=? WHERE plant_id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            ps.setInt(8, p.getPlantId());
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Plant p) throws SQLException {
        ps.setInt(1, p.getGardenId());
        ps.setString(2, p.getPlantName());
        ps.setString(3, p.getCategory());
        ps.setInt(4, p.getQuantity());
        ps.setInt(5, p.getWateringFrequencyDays());
        ps.setDouble(6, p.getWaterRequirementLiters());
        ps.setString(7, p.getDescription());
    }

    public void delete(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM plants WHERE plant_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Total number of individual plants (sum of quantities). */
    public int totalPlants() {
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT COALESCE(SUM(quantity),0) FROM plants"); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { throw new DataAccessException("totalPlants failed", e); }
    }
}
