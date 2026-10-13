package com.smartcampus.servlet;

import com.smartcampus.model.Plant;
import com.smartcampus.service.GardenService;
import com.smartcampus.service.PlantService;
import com.smartcampus.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/plants")
public class PlantServlet extends BaseServlet {
    private static final List<String> CATEGORIES = List.of("Tree", "Shrub", "Flower", "Herb", "Grass", "Vegetable", "Ornamental", "Fruit", "Other");
    private final PlantService service = new PlantService();
    private final GardenService gardenService = new GardenService();

    @Override protected String basePath() { return "/plants"; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int gardenId = Validator.intOrZero(req.getParameter("gardenId"));
        String category = Validator.trim(req.getParameter("category"));
        req.setAttribute("plants", service.findAll(gardenId, category));
        req.setAttribute("gardens", gardenService.findAll());
        req.setAttribute("categories", CATEGORIES);
        req.setAttribute("filterGardenId", gardenId);
        req.setAttribute("filterCategory", category);
        render(req, resp, "plants", "Plants", "plants");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireAdmin(req, resp)) return;
        int actor = currentUser(req).getUserId();
        String action = Validator.trim(req.getParameter("action"));
        if ("delete".equals(action)) {
            service.delete(Validator.intOrZero(req.getParameter("id")), actor);
            flash(req, "success", "Plant record deleted.");
            redirect(req, resp, basePath());
            return;
        }
        if (!"create".equals(action) && !"update".equals(action)) { redirect(req, resp, basePath()); return; }

        List<String> errors = new ArrayList<>();
        Plant p = new Plant();
        p.setGardenId(Validator.integer(req.getParameter("gardenId"), "Garden", 1, Integer.MAX_VALUE, errors));
        p.setPlantName(Validator.text(req.getParameter("plantName"), "Plant name", 1, 100, errors));
        p.setCategory(Validator.text(req.getParameter("category"), "Category", 1, 50, errors));
        p.setQuantity(Validator.integer(req.getParameter("quantity"), "Quantity", 1, 100_000, errors));
        p.setWateringFrequencyDays(Validator.integer(req.getParameter("wateringFrequencyDays"), "Watering frequency (days)", 1, 60, errors));
        p.setWaterRequirementLiters(Validator.decimal(req.getParameter("waterRequirementLiters"), "Water requirement (litres per plant)", 0.01, 100_000, errors));
        p.setDescription(Validator.optionalText(req.getParameter("description"), "Description", 1000, errors));
        if ("update".equals(action)) p.setPlantId(Validator.intOrZero(req.getParameter("id")));
        if (!errors.isEmpty()) { fail(req, resp, errors); return; }

        if ("create".equals(action)) { service.create(p, actor); flash(req, "success", "Plant added."); }
        else { service.update(p, actor); flash(req, "success", "Plant updated."); }
        redirect(req, resp, basePath());
    }
}
