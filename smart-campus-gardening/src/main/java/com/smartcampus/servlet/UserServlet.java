package com.smartcampus.servlet;

import com.smartcampus.model.User;
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

/** Admin-only (enforced by AuthFilter and again here). */
@WebServlet("/users")
public class UserServlet extends BaseServlet {
    private static final Set<String> ROLES = Set.of("ADMIN", "STAFF");
    private static final Set<String> STATUSES = Set.of("ACTIVE", "INACTIVE");
    private final UserService service = new UserService();

    @Override protected String basePath() { return "/users"; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireAdmin(req, resp)) return;
        req.setAttribute("users", service.findAll());
        render(req, resp, "users", "User Management", "users");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireAdmin(req, resp)) return;
        int actor = currentUser(req).getUserId();
        String action = Validator.trim(req.getParameter("action"));
        int id = Validator.intOrZero(req.getParameter("id"));

        switch (action) {
            case "activate":   service.setActive(id, true, actor);  flash(req, "success", "User activated."); break;
            case "deactivate": service.setActive(id, false, actor); flash(req, "success", "User deactivated. They can no longer sign in."); break;
            case "resetPassword": {
                List<String> errors = new ArrayList<>();
                String pw = req.getParameter("newPassword");
                Validator.passwordOk(pw, errors);
                if (!errors.isEmpty()) { fail(req, resp, errors); return; }
                service.resetPassword(id, pw, actor);
                flash(req, "success", "Password reset.");
                break;
            }
            case "create":
            case "update": {
                List<String> errors = new ArrayList<>();
                User u = new User();
                u.setFullName(Validator.text(req.getParameter("fullName"), "Full name", 2, 100, errors));
                u.setEmail(Validator.email(req.getParameter("email"), errors));
                u.setPhone(Validator.optionalText(req.getParameter("phone"), "Phone", 20, errors));
                u.setRole(Validator.oneOf(req.getParameter("role"), ROLES, "role", errors));
                u.setStatus(Validator.oneOf(req.getParameter("status"), STATUSES, "status", errors));
                String pw = req.getParameter("password");
                if ("create".equals(action)) Validator.passwordOk(pw, errors);
                if ("update".equals(action)) u.setUserId(id);
                if (!errors.isEmpty()) { fail(req, resp, errors); return; }
                if ("create".equals(action)) { service.create(u, pw, actor); flash(req, "success", "User created."); }
                else { service.update(u, actor); flash(req, "success", "User updated."); }
                break;
            }
            default: break;
        }
        redirect(req, resp, basePath());
    }
}
