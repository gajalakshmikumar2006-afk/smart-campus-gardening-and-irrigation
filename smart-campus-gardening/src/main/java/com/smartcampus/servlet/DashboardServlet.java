package com.smartcampus.servlet;

import com.smartcampus.model.IrrigationSchedule;
import com.smartcampus.model.MaintenanceTask;
import com.smartcampus.model.Notification;
import com.smartcampus.model.User;
import com.smartcampus.service.AnalyticsService;
import com.smartcampus.service.IrrigationService;
import com.smartcampus.service.MaintenanceService;
import com.smartcampus.service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet {
    private final AnalyticsService analytics = new AnalyticsService();
    private final IrrigationService irrigation = new IrrigationService();
    private final MaintenanceService maintenance = new MaintenanceService();
    private final NotificationService notifications = new NotificationService();

    @Override protected String basePath() { return "/dashboard"; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = currentUser(req);
        for (Map.Entry<String, Object> e : analytics.dashboard().entrySet()) req.setAttribute(e.getKey(), e.getValue());

        List<IrrigationSchedule> up = irrigation.upcoming(u);
        req.setAttribute("upcomingList", up.subList(0, Math.min(5, up.size())));

        List<MaintenanceTask> open = new ArrayList<>();
        for (MaintenanceTask t : maintenance.list(u, null)) if (!"COMPLETED".equals(t.getStatus())) open.add(t);
        req.setAttribute("openTasks", open.subList(0, Math.min(5, open.size())));

        List<Notification> unread = new ArrayList<>();
        for (Notification n : notifications.list(u.getUserId())) if (!n.isRead()) unread.add(n);
        req.setAttribute("unreadList", unread.subList(0, Math.min(5, unread.size())));
        render(req, resp, "dashboard", "Dashboard", "dashboard");
    }
}
