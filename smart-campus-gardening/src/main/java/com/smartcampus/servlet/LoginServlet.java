package com.smartcampus.servlet;

import com.smartcampus.model.User;
import com.smartcampus.service.UserService;
import com.smartcampus.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_MS = 5 * 60 * 1000L;
    /** key = email|ip  ->  {failed attempts, lock-until epoch ms} */
    private static final ConcurrentHashMap<String, long[]> FAILS = new ConcurrentHashMap<>();
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        if (s != null && s.getAttribute("user") != null) { resp.sendRedirect(req.getContextPath() + "/dashboard"); return; }
        if ("1".equals(req.getParameter("expired"))) req.setAttribute("info", "Your session ended or your account was deactivated. Please sign in again.");
        if ("1".equals(req.getParameter("loggedout"))) req.setAttribute("info", "You have been signed out.");
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = Validator.trim(req.getParameter("email")).toLowerCase();
        String password = req.getParameter("password");
        String key = email + "|" + req.getRemoteAddr();
        long[] f = FAILS.get(key);
        if (f != null && f[1] > System.currentTimeMillis()) {
            req.setAttribute("error", "Too many failed attempts. Please wait a few minutes and try again.");
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        User user = (email.isEmpty() || password == null || password.isEmpty()) ? null : userService.authenticate(email, password);
        if (user == null) {
            long[] rec = FAILS.computeIfAbsent(key, k -> new long[2]);
            synchronized (rec) {
                rec[0]++;
                if (rec[0] >= MAX_ATTEMPTS) { rec[1] = System.currentTimeMillis() + LOCK_MS; rec[0] = 0; }
            }
            req.setAttribute("error", "Invalid email or password, or the account is inactive.");
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        FAILS.remove(key);
        HttpSession old = req.getSession(false);
        if (old != null) old.invalidate();                    // prevents session fixation
        HttpSession session = req.getSession(true);
        session.setMaxInactiveInterval(30 * 60);
        session.setAttribute("user", user);
        session.setAttribute("csrfToken", UUID.randomUUID().toString());
        userService.logAuth(user, user.getFullName() + " signed in");
        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }
}
