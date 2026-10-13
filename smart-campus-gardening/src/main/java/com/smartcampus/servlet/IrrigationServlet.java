package com.smartcampus.servlet;

import com.google.gson.Gson;
import com.smartcampus.model.IrrigationSchedule;
import com.smartcampus.model.User;
import com.smartcampus.service.GardenService;
import com.smartcampus.service.IrrigationService;
import com.smartcampus.service.UserService;
import com.smartcampus.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@WebServlet("/irrigation")
public class IrrigationServlet extends BaseServlet {
    private static final Set<String> EDIT_STATUSES = Set.of("PENDING", "RESCHEDULED");
    private static final Set<String> CHANGE_STATUSES = Set.of("PENDING", "COMPLETED", "RESCHEDULED");
    private final IrrigationService service = new IrrigationService();
    private final GardenService gardenService = new GardenService();
    private final UserService userService = new UserService();
    private final Gson gson = new Gson();

    @Override protected String basePath() { return "/irrigation"; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = currentUser(req);
        if ("recommend".equals(req.getParameter("action"))) {          // JSON used by the schedule form
            int gardenId = Validator.intOrZero(req.getParameter("gardenId"));
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(gson.toJson(service.recommend(gardenId)));
            return;
        }
        req.setAttribute("upcoming", service.upcoming(u));
        req.setAttribute("history", service.history(u));
        req.setAttribute("recommendations", service.recommendAll());
        req.setAttribute("gardens", gardenService.findAll());
        req.setAttribute("staff", userService.findActiveStaff());
        render(req, resp, "irrigation", "Irrigation Scheduling", "irrigation");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = currentUser(req);
        String action = Validator.trim(req.getParameter("action"));

        if ("status".equals(action)) {                                    // admin or assigned staff
            List<String> errors = new ArrayList<>();
            String status = Validator.oneOf(req.getParameter("status"), CHANGE_STATUSES, "status", errors);
            Double liters = null;
            String litersRaw = Validator.trim(req.getParameter("liters"));
            if (!litersRaw.isEmpty()) liters = Validator.decimal(litersRaw, "Water used (litres)", 0.01, 10_000_000, errors);
            String notes = Validator.optionalText(req.getParameter("notes"), "Notes", 255, errors);
            if (!errors.isEmpty()) { fail(req, resp, errors); return; }
            service.changeStatus(Validator.intOrZero(req.getParameter("id")), status, liters, notes, u);
            flash(req, "success", "Irrigation status updated to " + status.toLowerCase() + ".");
            redirect(req, resp, basePath());
            return;
        }

        if (!requireAdmin(req, resp)) return;
        if ("cancel".equals(action)) {
            service.cancel(Validator.intOrZero(req.getParameter("id")), u);
            flash(req, "success", "Irrigation schedule cancelled.");
            redirect(req, resp, basePath());
            return;
        }
        if (!"create".equals(action) && !"update".equals(action)) { redirect(req, resp, basePath()); return; }

        List<String> errors = new ArrayList<>();
        IrrigationSchedule s = new IrrigationSchedule();
        s.setGardenId(Validator.integer(req.getParameter("gardenId"), "Garden", 1, Integer.MAX_VALUE, errors));
        s.setScheduledAt(Validator.dateTime(req.getParameter("scheduleDate"), req.getParameter("scheduleTime"), "Schedule", errors));
        s.setEstimatedWaterLiters(Validator.decimal(req.getParameter("estimatedLiters"), "Estimated water (litres)", 0.1, 10_000_000, errors));
        int staffId = Validator.intOrZero(req.getParameter("assignedUserId"));
        s.setAssignedUserId(staffId > 0 ? staffId : null);
        s.setNotes(Validator.optionalText(req.getParameter("notes"), "Notes", 255, errors));
        s.setStatus("update".equals(action)
                ? Validator.oneOf(req.getParameter("status"), EDIT_STATUSES, "status", errors) : "PENDING");
        if ("update".equals(action)) s.setScheduleId(Validator.intOrZero(req.getParameter("id")));
        if (!errors.isEmpty()) { fail(req, resp, errors); return; }

        if ("create".equals(action)) { service.create(s, u); flash(req, "success", "Irrigation scheduled."); }
        else { service.update(s, u); flash(req, "success", "Irrigation schedule updated."); }
        redirect(req, resp, basePath());
    }
}
