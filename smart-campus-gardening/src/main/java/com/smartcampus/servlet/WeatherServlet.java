package com.smartcampus.servlet;

import com.smartcampus.model.User;
import com.smartcampus.model.WeatherRecord;
import com.smartcampus.service.WeatherService;
import com.smartcampus.util.BusinessException;
import com.smartcampus.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@WebServlet("/weather")
public class WeatherServlet extends BaseServlet {
    private static final Pattern LOCATION = Pattern.compile("^[\\p{L}\\p{N} ,.'-]{2,100}$");
    private final WeatherService service = new WeatherService();

    @Override protected String basePath() { return "/weather"; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WeatherRecord w = service.latest();
        if (service.isStale(w)) {
            try { w = service.refresh(); }
            catch (BusinessException e) { req.setAttribute("weatherNotice", e.getMessage()); }   // fallback message
        }
        double rain = w == null ? 0 : w.getRainMmCurrent() + w.getRainMmForecast24h();
        req.setAttribute("weather", w);
        req.setAttribute("forecast", service.forecastOf(w));
        req.setAttribute("campusLocation", service.campusLocation());
        req.setAttribute("apiKeyConfigured", service.apiKeyConfigured());
        req.setAttribute("rainTotal24h", rain);
        req.setAttribute("postponeAdvice", rain >= 5.0);
        render(req, resp, "weather", "Weather", "weather");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = currentUser(req);
        String action = Validator.trim(req.getParameter("action"));
        if ("location".equals(action)) {
            if (!requireAdmin(req, resp)) return;
            String loc = Validator.trim(req.getParameter("location"));
            List<String> errors = new ArrayList<>();
            if (!LOCATION.matcher(loc).matches()) errors.add("Enter a valid location such as \"Chennai,IN\" (letters, numbers, comma, space).");
            if (!errors.isEmpty()) { fail(req, resp, errors); return; }
            service.saveLocation(loc, u.getUserId());
            try { service.refresh(); flash(req, "success", "Campus location saved and weather refreshed."); }
            catch (BusinessException e) { flash(req, "warning", "Location saved, but weather could not be refreshed: " + e.getMessage()); }
        } else if ("refresh".equals(action)) {
            WeatherRecord last = service.latest();
            boolean tooSoon = last != null && last.getRetrievedAt().getTime() > System.currentTimeMillis() - 60_000
                    && last.getCampusLocation().equals(service.campusLocation());
            if (tooSoon) flash(req, "info", "Weather was refreshed less than a minute ago. Please wait a moment.");
            else { service.refresh(); flash(req, "success", "Weather refreshed."); }
        }
        redirect(req, resp, basePath());
    }
}
