package com.smartcampus.filter;

import com.smartcampus.dao.UserDAO;
import com.smartcampus.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

/**
 * Single gatekeeper for every request:
 *  1. UTF-8 + security headers + no-cache for dynamic pages
 *  2. Authentication (session must contain an ACTIVE user, re-checked in the database)
 *  3. Authorization for admin-only URLs (/users)
 *  4. CSRF token check on every POST except /login
 */
@WebFilter("/*")
public class AuthFilter implements Filter {
    private final UserDAO userDAO = new UserDAO();

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) rq;
        HttpServletResponse resp = (HttpServletResponse) rs;
        req.setCharacterEncoding("UTF-8");

        String path = req.getRequestURI().substring(req.getContextPath().length());
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Referrer-Policy", "same-origin");

        if (path.startsWith("/assets/")) { chain.doFilter(rq, rs); return; }
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        resp.setHeader("Pragma", "no-cache");

        if (path.equals("/login")) { chain.doFilter(rq, rs); return; }

        HttpSession session = req.getSession(false);
        User sessionUser = session == null ? null : (User) session.getAttribute("user");
        if (sessionUser == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }

        // Re-read the user so that deactivation / role changes take effect immediately.
        User fresh = userDAO.findById(sessionUser.getUserId());
        if (fresh == null || !fresh.isActive()) {
            session.invalidate();
            resp.sendRedirect(req.getContextPath() + "/login?expired=1");
            return;
        }
        session.setAttribute("user", fresh);

        if ((path.equals("/users") || path.startsWith("/users/")) && !fresh.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Administrator access required.");
            return;
        }

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String expected = (String) session.getAttribute("csrfToken");
            String actual = req.getParameter("_csrf");
            if (expected == null || !expected.equals(actual)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing security token. Reload the page and try again.");
                return;
            }
        }
        chain.doFilter(rq, rs);
    }
}
