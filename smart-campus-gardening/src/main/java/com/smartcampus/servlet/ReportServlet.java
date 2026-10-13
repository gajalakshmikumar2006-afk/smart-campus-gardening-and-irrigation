package com.smartcampus.servlet;

import com.smartcampus.model.ReportRow;
import com.smartcampus.service.AnalyticsService;
import com.smartcampus.service.GardenService;
import com.smartcampus.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@WebServlet("/reports")
public class ReportServlet extends BaseServlet {
    private static final Set<String> TYPES = Set.of("ALL", "IRRIGATION", "MAINTENANCE");
    private final AnalyticsService service = new AnalyticsService();
    private final GardenService gardenService = new GardenService();

    @Override protected String basePath() { return "/reports"; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LocalDate from = Validator.optionalDate(req.getParameter("from"));
        LocalDate to = Validator.optionalDate(req.getParameter("to"));
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now().plusDays(30);
        if (from.isAfter(to)) { LocalDate tmp = from; from = to; to = tmp; }
        int gardenId = Validator.intOrZero(req.getParameter("gardenId"));
        String type = Validator.trim(req.getParameter("type")).toUpperCase();
        if (!TYPES.contains(type)) type = "ALL";

        List<ReportRow> rows = service.report(from, to, gardenId, type);
        if ("csv".equals(req.getParameter("format"))) { writeCsv(resp, rows); return; }

        double litres = 0;
        int irrigationCount = 0, taskCount = 0;
        for (ReportRow r : rows) {
            if ("IRRIGATION".equals(r.getActivityType())) {
                irrigationCount++;
                String d = r.getDetails();                         // "Irrigation completed - 123.4 L (estimated)"
                try { litres += Double.parseDouble(d.substring(d.indexOf('-') + 1, d.indexOf(" L")).trim()); } catch (RuntimeException ignored) { }
            } else taskCount++;
        }
        req.setAttribute("rows", rows);
        req.setAttribute("from", from);
        req.setAttribute("to", to);
        req.setAttribute("filterGardenId", gardenId);
        req.setAttribute("filterType", type);
        req.setAttribute("irrigationCount", irrigationCount);
        req.setAttribute("taskCount", taskCount);
        req.setAttribute("reportLitres", Math.round(litres * 10.0) / 10.0);
        req.setAttribute("gardens", gardenService.findAll());
        render(req, resp, "reports", "Reports", "reports");
    }

    private void writeCsv(HttpServletResponse resp, List<ReportRow> rows) throws IOException {
        resp.setContentType("text/csv;charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"campus-garden-report-" + LocalDate.now() + ".csv\"");
        PrintWriter out = resp.getWriter();
        out.write('\uFEFF');                                        // BOM so Excel reads UTF-8 correctly
        out.print("Date,Activity Type,Garden,Details,Staff,Status\r\n");
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (ReportRow r : rows) {
            out.print(csv(f.format(r.getActivityDate().toLocalDateTime())) + "," + csv(r.getActivityType()) + "," + csv(r.getGardenName()) + ","
                    + csv(r.getDetails()) + "," + csv(r.getStaffName()) + "," + csv(r.getStatus()) + "\r\n");
        }
        out.flush();
    }

    /** Quotes a CSV cell and neutralises spreadsheet formula injection (cells starting with = + - @). */
    private static String csv(String s) {
        if (s == null) return "\"\"";
        String v = s;
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) v = "'" + v;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
