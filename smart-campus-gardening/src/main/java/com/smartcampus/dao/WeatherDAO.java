package com.smartcampus.dao;

import com.smartcampus.model.WeatherRecord;
import com.smartcampus.util.DBUtil;
import com.smartcampus.util.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WeatherDAO {
    public void insert(WeatherRecord w) {
        String sql = "INSERT INTO weather_records (campus_location, temperature_c, conditions, humidity, rain_mm_current, rain_mm_forecast_24h, forecast_summary, is_demo) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, w.getCampusLocation());
            ps.setDouble(2, w.getTemperatureC());
            ps.setString(3, w.getConditions());
            ps.setInt(4, w.getHumidity());
            ps.setDouble(5, w.getRainMmCurrent());
            ps.setDouble(6, w.getRainMmForecast24h());
            ps.setString(7, w.getForecastSummary());
            ps.setBoolean(8, w.isDemo());
            ps.executeUpdate();
        } catch (SQLException e) { throw new DataAccessException("Weather insert failed", e); }
    }

    public WeatherRecord findLatest() {
        String sql = "SELECT weather_id, campus_location, temperature_c, conditions, humidity, rain_mm_current, rain_mm_forecast_24h, forecast_summary, is_demo, retrieved_at " +
                     "FROM weather_records ORDER BY retrieved_at DESC, weather_id DESC LIMIT 1";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return null;
            WeatherRecord w = new WeatherRecord();
            w.setWeatherId(rs.getInt(1));
            w.setCampusLocation(rs.getString(2));
            w.setTemperatureC(rs.getDouble(3));
            w.setConditions(rs.getString(4));
            w.setHumidity(rs.getInt(5));
            w.setRainMmCurrent(rs.getDouble(6));
            w.setRainMmForecast24h(rs.getDouble(7));
            w.setForecastSummary(rs.getString(8));
            w.setDemo(rs.getBoolean(9));
            w.setRetrievedAt(rs.getTimestamp(10));
            return w;
        } catch (SQLException e) { throw new DataAccessException("Weather findLatest failed", e); }
    }
}
