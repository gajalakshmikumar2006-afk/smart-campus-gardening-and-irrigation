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

@WebServlet("/profile")
public class ProfileServlet extends BaseServlet {
    private final UserService service = new UserService();

    @Override protected String basePath() { return "/profile"; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        render(req, resp, "profile", "My Profile", "profile");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = currentUser(req);
        String action = Validator.trim(req.getParameter("action"));
        List<String> errors = new ArrayList<>();
        if ("profile".equals(action)) {
            String name = Validator.text(req.getParameter("fullName"), "Full name", 2, 100, errors);
            String phone = Validator.optionalText(req.getParameter("phone"), "Phone", 20, errors);
            if (!errors.isEmpty()) { fail(req, resp, errors); return; }
            service.updateProfile(u.getUserId(), name, phone);
            flash(req, "success", "Profile updated.");
        } else if ("password".equals(action)) {
            String current = req.getParameter("currentPassword");
            String pw = req.getParameter("newPassword");
            String confirm = req.getParameter("confirmPassword");
            Validator.passwordOk(pw, errors);
            if (pw != null && !pw.equals(confirm)) errors.add("New password and confirmation do not match.");
            if (!errors.isEmpty()) { fail(req, resp, errors); return; }
            service.changeOwnPassword(u.getUserId(), current, pw);
            flash(req, "success", "Password changed.");
        }
        redirect(req, resp, basePath());
    }
}
