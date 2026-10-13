package com.smartcampus.servlet;

import com.smartcampus.service.AnalyticsService;
import com.smartcampus.service.GardenService;
import com.smartcampus.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@WebServlet("/water-analytics")
public class WaterAnalyticsServlet extends BaseServlet {
    private final AnalyticsService service = new AnalyticsService();
    private final GardenService gardenService = new GardenService();

    @Override protected String basePath() { return "/water-analytics"; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LocalDate today = LocalDate.now();
        LocalDate to = Validator.optionalDate(req.getParameter("to"));
        LocalDate from = Validator.optionalDate(req.getParameter("from"));
        if (to == null) to = today;
        if (from == null) from = to.minusDays(29);
        if (from.isAfter(to)) { LocalDate tmp = from; from = to; to = tmp; }
        if (ChronoUnit.DAYS.between(from, to) > 730) from = to.minusDays(730);       // keep queries bounded
        int gardenId = Validator.intOrZero(req.getParameter("gardenId"));

        for (Map.Entry<String, Object> e : service.waterAnalytics(from, to, gardenId).entrySet()) req.setAttribute(e.getKey(), e.getValue());
        req.setAttribute("from", from);
        req.setAttribute("to", to);
        req.setAttribute("filterGardenId", gardenId);
        req.setAttribute("gardens", gardenService.findAll());
        render(req, resp, "water-analytics", "Water Usage & Analytics", "analytics");
    }
}
