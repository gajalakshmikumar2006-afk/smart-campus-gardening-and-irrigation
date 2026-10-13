package com.smartcampus.servlet;

import com.smartcampus.model.Garden;
import com.smartcampus.service.GardenService;
import com.smartcampus.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@WebServlet("/gardens")
public class GardenServlet extends BaseServlet {
    private static final Set<String> STATUSES = Set.of("ACTIVE", "UNDER_MAINTENANCE", "INACTIVE");
    private static final List<String> TYPES = List.of("Lawn", "Flower", "Herbal", "Vegetable", "Ornamental", "Orchard", "Other");
    private final GardenService service = new GardenService();

    @Override protected String basePath() { return "/gardens"; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("gardens", service.findAll());
        req.setAttribute("gardenTypes", TYPES);
        render(req, resp, "gardens", "Gardens", "gardens");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireAdmin(req, resp)) return;
        int actor = currentUser(req).getUserId();
        String action = Validator.trim(req.getParameter("action"));
        if ("delete".equals(action)) {
            service.delete(Validator.intOrZero(req.getParameter("id")), actor);
            flash(req, "success", "Garden deleted along with its plants, schedules and tasks.");
            redirect(req, resp, basePath());
            return;
        }
        if (!"create".equals(action) && !"update".equals(action)) { redirect(req, resp, basePath()); return; }

        List<String> errors = new ArrayList<>();
        Garden g = new Garden();
        g.setGardenName(Validator.text(req.getParameter("gardenName"), "Garden name", 1, 100, errors));
        g.setLocation(Validator.text(req.getParameter("location"), "Location", 1, 150, errors));
        g.setAreaSqm(Validator.decimal(req.getParameter("areaSqm"), "Area (sq m)", 0.01, 1_000_000, errors));
        g.setGardenType(Validator.text(req.getParameter("gardenType"), "Garden type", 1, 50, errors));
        g.setDescription(Validator.optionalText(req.getParameter("description"), "Description", 1000, errors));
        g.setStatus(Validator.oneOf(req.getParameter("status"), STATUSES, "status", errors));
        if ("update".equals(action)) g.setGardenId(Validator.intOrZero(req.getParameter("id")));
        if (!errors.isEmpty()) { fail(req, resp, errors); return; }

        if ("create".equals(action)) { service.create(g, actor); flash(req, "success", "Garden \"" + g.getGardenName() + "\" added."); }
        else { service.update(g, actor); flash(req, "success", "Garden updated."); }
        redirect(req, resp, basePath());
    }
}
