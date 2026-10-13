package com.smartcampus.servlet;

import com.smartcampus.model.Notification;
import com.smartcampus.model.User;
import com.smartcampus.service.IrrigationService;
import com.smartcampus.service.MaintenanceService;
import com.smartcampus.service.NotificationService;
import com.smartcampus.model.MaintenanceTask;
import com.smartcampus.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/notifications")
public class NotificationServlet extends BaseServlet {
    private final NotificationService service = new NotificationService();
    private final IrrigationService irrigation = new IrrigationService();
    private final MaintenanceService maintenance = new MaintenanceService();

    @Override protected String basePath() { return "/notifications"; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = currentUser(req);
        List<Notification> all = service.list(u.getUserId());
        List<Notification> unread = new ArrayList<>(), read = new ArrayList<>();
        for (Notification n : all) (n.isRead() ? read : unread).add(n);
        req.setAttribute("unread", unread);
        req.setAttribute("read", read);
        req.setAttribute("dueSoon", irrigation.dueSoon(u, 48));           // upcoming irrigation reminders (next 48 h)
        List<MaintenanceTask> pending = new ArrayList<>();
        for (MaintenanceTask t : maintenance.list(u, null)) if (!"COMPLETED".equals(t.getStatus())) pending.add(t);
        req.setAttribute("pendingTasks", pending);
        render(req, resp, "notifications", "Notifications", "notifications");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = currentUser(req);
        String action = Validator.trim(req.getParameter("action"));
        if ("read".equals(action)) service.markRead(Validator.intOrZero(req.getParameter("id")), u.getUserId());
        else if ("readAll".equals(action)) { service.markAllRead(u.getUserId()); flash(req, "success", "All notifications marked as read."); }
        redirect(req, resp, basePath());
    }
}
