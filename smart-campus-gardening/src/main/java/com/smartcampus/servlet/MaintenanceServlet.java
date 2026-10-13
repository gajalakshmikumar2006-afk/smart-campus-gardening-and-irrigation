package com.smartcampus.servlet;

import com.smartcampus.model.MaintenanceTask;
import com.smartcampus.model.User;
import com.smartcampus.service.GardenService;
import com.smartcampus.service.MaintenanceService;
import com.smartcampus.service.UserService;
import com.smartcampus.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@WebServlet("/maintenance")
public class MaintenanceServlet extends BaseServlet {
    private static final Set<String> STATUSES = Set.of("PENDING", "IN_PROGRESS", "COMPLETED");
    private static final Set<String> PRIORITIES = Set.of("LOW", "MEDIUM", "HIGH");
    private final MaintenanceService service = new MaintenanceService();
    private final GardenService gardenService = new GardenService();
    private final UserService userService = new UserService();

    @Override protected String basePath() { return "/maintenance"; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = currentUser(req);
        String status = Validator.trim(req.getParameter("status")).toUpperCase();
        if (!STATUSES.contains(status)) status = "";
        List<MaintenanceTask> tasks = service.list(u, status);
        int overdue = 0, dueSoon = 0;
        LocalDate today = LocalDate.now();
        for (MaintenanceTask t : tasks) {
            if (t.isOverdue()) overdue++;
            else if (!"COMPLETED".equals(t.getStatus()) && !t.getDueDate().toLocalDate().isAfter(today.plusDays(7))) dueSoon++;
        }
        req.setAttribute("tasks", tasks);
        req.setAttribute("overdueCount", overdue);
        req.setAttribute("dueSoonCount", dueSoon);
        req.setAttribute("filterStatus", status);
        req.setAttribute("gardens", gardenService.findAll());
        req.setAttribute("staff", userService.findActiveStaff());
        render(req, resp, "maintenance", "Maintenance", "maintenance");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = currentUser(req);
        String action = Validator.trim(req.getParameter("action"));

        if ("status".equals(action)) {                               // admin or assigned staff
            List<String> errors = new ArrayList<>();
            String status = Validator.oneOf(req.getParameter("status"), STATUSES, "status", errors);
            if (!errors.isEmpty()) { fail(req, resp, errors); return; }
            service.changeStatus(Validator.intOrZero(req.getParameter("id")), status, u);
            flash(req, "success", "Task status updated.");
            redirect(req, resp, basePath());
            return;
        }

        if (!requireAdmin(req, resp)) return;
        if ("delete".equals(action)) {
            service.delete(Validator.intOrZero(req.getParameter("id")), u);
            flash(req, "success", "Task deleted.");
            redirect(req, resp, basePath());
            return;
        }
        if (!"create".equals(action) && !"update".equals(action)) { redirect(req, resp, basePath()); return; }

        List<String> errors = new ArrayList<>();
        MaintenanceTask t = new MaintenanceTask();
        t.setGardenId(Validator.integer(req.getParameter("gardenId"), "Garden", 1, Integer.MAX_VALUE, errors));
        int staffId = Validator.intOrZero(req.getParameter("assignedUserId"));
        t.setAssignedUserId(staffId > 0 ? staffId : null);
        t.setTitle(Validator.text(req.getParameter("title"), "Title", 3, 150, errors));
        t.setDescription(Validator.optionalText(req.getParameter("description"), "Description", 2000, errors));
        t.setPriority(Validator.oneOf(req.getParameter("priority"), PRIORITIES, "priority", errors));
        t.setDueDate(Validator.date(req.getParameter("dueDate"), "Due date", errors));
        t.setStatus("update".equals(action) ? Validator.oneOf(req.getParameter("status"), STATUSES, "status", errors) : "PENDING");
        if ("update".equals(action)) t.setTaskId(Validator.intOrZero(req.getParameter("id")));
        if (!errors.isEmpty()) { fail(req, resp, errors); return; }

        if ("create".equals(action)) { service.create(t, u); flash(req, "success", "Task created."); }
        else { service.update(t, u); flash(req, "success", "Task updated."); }
        redirect(req, resp, basePath());
    }
}
