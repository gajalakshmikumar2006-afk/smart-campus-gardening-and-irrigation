package com.smartcampus.servlet;

import com.smartcampus.model.User;
import com.smartcampus.service.NotificationService;
import com.smartcampus.util.BusinessException;
import com.smartcampus.util.DataAccessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/** Shared helpers: rendering, flash messages, role checks and safe error handling. */
public abstract class BaseServlet extends HttpServlet {
    private final NotificationService notificationService = new NotificationService();

    /** URL (after the context path) users are sent back to after a POST, e.g. "/gardens". */
    protected abstract String basePath();

    protected static User currentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (User) s.getAttribute("user");
    }

    protected void render(HttpServletRequest req, HttpServletResponse resp, String view, String title, String activePage)
            throws ServletException, IOException {
        User u = currentUser(req);
        req.setAttribute("pageTitle", title);
        req.setAttribute("activePage", activePage);
        if (u != null) req.setAttribute("unreadCount", notificationService.unreadCount(u.getUserId()));
        HttpSession s = req.getSession(false);
        if (s != null && s.getAttribute("flashMsg") != null) {
            req.setAttribute("flashMsg", s.getAttribute("flashMsg"));
            req.setAttribute("flashType", s.getAttribute("flashType"));
            s.removeAttribute("flashMsg");
            s.removeAttribute("flashType");
        }
        req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, resp);
    }

    protected void flash(HttpServletRequest req, String type, String message) {
        req.getSession().setAttribute("flashType", type);   // success | danger | warning | info
        req.getSession().setAttribute("flashMsg", message);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    /** Flash a list of validation errors and go back to the module page. */
    protected void fail(HttpServletRequest req, HttpServletResponse resp, List<String> errors) throws IOException {
        flash(req, "danger", String.join(" ", errors));
        redirect(req, resp, basePath());
    }

    /** Returns false (after redirecting) when the user is not an administrator. */
    protected boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User u = currentUser(req);
        if (u != null && u.isAdmin()) return true;
        flash(req, "danger", "Only administrators can perform that action.");
        redirect(req, resp, "/dashboard");
        return false;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            super.service(req, resp);
        } catch (BusinessException e) {
            handle(req, resp, e.getMessage(), "warning");
        } catch (DataAccessException e) {
            // Details go to the server log only - never to the browser.
            getServletContext().log("Database error in " + getClass().getSimpleName(), e);
            handle(req, resp, "A database error occurred. Please try again, or contact the administrator if it continues.", "danger");
        }
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp, String message, String type)
            throws ServletException, IOException {
        if ("GET".equals(req.getMethod())) {
            req.setAttribute("errorMessage", message);
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        } else {
            flash(req, type, message);
            redirect(req, resp, basePath());
        }
    }
}
