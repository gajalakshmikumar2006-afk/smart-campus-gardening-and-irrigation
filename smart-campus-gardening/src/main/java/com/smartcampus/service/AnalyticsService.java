package com.smartcampus.service;

import com.google.gson.Gson;
import com.smartcampus.dao.ActivityLogDAO;
import com.smartcampus.dao.AnalyticsDAO;
import com.smartcampus.dao.GardenDAO;
import com.smartcampus.dao.IrrigationDAO;
import com.smartcampus.dao.MaintenanceDAO;
import com.smartcampus.dao.PlantDAO;
import com.smartcampus.dao.WeatherDAO;
import com.smartcampus.model.LabelValue;
import com.smartcampus.model.ReportRow;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Builds dashboard figures, water analytics and reports. All water volumes are ESTIMATES (no flow sensors). */
public class AnalyticsService {
    private static final Gson GSON = new Gson();
    private final GardenDAO gardenDAO = new GardenDAO();
    private final PlantDAO plantDAO = new PlantDAO();
    private final IrrigationDAO irrigationDAO = new IrrigationDAO();
    private final MaintenanceDAO maintenanceDAO = new MaintenanceDAO();
    private final WeatherDAO weatherDAO = new WeatherDAO();
    private final ActivityLogDAO logDAO = new ActivityLogDAO();
    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();

    public static String labelsJson(List<LabelValue> l) {
        List<String> a = new ArrayList<>();
        for (LabelValue x : l) a.add(x.getLabel());
        return GSON.toJson(a);
    }

    public static String valuesJson(List<LabelValue> l) {
        List<Double> a = new ArrayList<>();
        for (LabelValue x : l) a.add(x.getValue());
        return GSON.toJson(a);
    }

    /** Key figures and chart data for dashboard.jsp (keys become request attributes). */
    public Map<String, Object> dashboard() {
        Map<String, Object> m = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        m.put("totalGardens", gardenDAO.count());
        m.put("totalPlants", plantDAO.totalPlants());
        m.put("upcomingIrrigations", irrigationDAO.countUpcoming());
        m.put("completedIrrigations", irrigationDAO.countCompleted());
        m.put("pendingTasks", maintenanceDAO.countByStatusValue("PENDING"));
        m.put("overdueTasks", maintenanceDAO.countOverdue(null));
        m.put("totalWater", Math.round(analyticsDAO.totals(LocalDate.of(2000, 1, 1), today, 0)[0] * 10.0) / 10.0);
        m.put("recentActivities", logDAO.findRecent(8));
        m.put("weather", weatherDAO.findLatest());

        List<LabelValue> monthly = analyticsDAO.usageOverTime("MONTHLY", today.withDayOfMonth(1).minusMonths(5), today, 0);
        m.put("monthlyLabels", labelsJson(monthly));
        m.put("monthlyValues", valuesJson(monthly));
        List<LabelValue> irr = irrigationDAO.countByStatus();
        m.put("irrigationLabels", labelsJson(irr));
        m.put("irrigationValues", valuesJson(irr));
        List<LabelValue> mnt = maintenanceDAO.countByStatus();
        m.put("taskLabels", labelsJson(mnt));
        m.put("taskValues", valuesJson(mnt));
        return m;
    }

    /** Data for water-analytics.jsp. granularity is validated by the caller. */
    public Map<String, Object> waterAnalytics(LocalDate from, LocalDate to, int gardenId) {
        Map<String, Object> m = new LinkedHashMap<>();
        double[] t = analyticsDAO.totals(from, to, gardenId);
        long days = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1);
        m.put("totalLiters", Math.round(t[0] * 10.0) / 10.0);
        m.put("activityCount", (int) t[1]);
        m.put("avgPerActivity", t[1] > 0 ? Math.round(t[0] / t[1] * 10.0) / 10.0 : 0.0);
        m.put("avgPerDay", Math.round(t[0] / days * 10.0) / 10.0);
        List<LabelValue> daily = analyticsDAO.usageOverTime("DAILY", from, to, gardenId);
        List<LabelValue> weekly = analyticsDAO.usageOverTime("WEEKLY", from, to, gardenId);
        List<LabelValue> monthly = analyticsDAO.usageOverTime("MONTHLY", from, to, gardenId);
        List<LabelValue> byGarden = analyticsDAO.usageByGarden(from, to, gardenId);
        m.put("daily", daily); m.put("weekly", weekly); m.put("monthly", monthly); m.put("byGarden", byGarden);
        m.put("dailyLabels", labelsJson(daily));   m.put("dailyValues", valuesJson(daily));
        m.put("weeklyLabels", labelsJson(weekly)); m.put("weeklyValues", valuesJson(weekly));
        m.put("monthlyLabels", labelsJson(monthly)); m.put("monthlyValues", valuesJson(monthly));
        m.put("gardenLabels", labelsJson(byGarden)); m.put("gardenValues", valuesJson(byGarden));
        return m;
    }

    public List<ReportRow> report(LocalDate from, LocalDate to, int gardenId, String type) {
        return analyticsDAO.report(from, to, gardenId, type);
    }
}
