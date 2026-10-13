package com.smartcampus.service;

import com.smartcampus.dao.ActivityLogDAO;
import com.smartcampus.dao.UserDAO;
import com.smartcampus.model.User;
import com.smartcampus.util.BusinessException;
import com.smartcampus.util.PasswordUtil;
import com.smartcampus.util.Tx;
import java.util.List;

public class UserService {
    private static final String DUMMY_HASH = PasswordUtil.hash("dummy-password-for-timing");
    private final UserDAO userDAO = new UserDAO();
    private final ActivityLogDAO logDAO = new ActivityLogDAO();

    /** Returns the user when credentials are valid AND the account is active; otherwise null. */
    public User authenticate(String email, String password) {
        User u = userDAO.findByEmail(email);
        if (u == null) { PasswordUtil.verify(password, DUMMY_HASH); return null; }   // keeps timing similar
        if (!PasswordUtil.verify(password, u.getPasswordHash()) || !u.isActive()) return null;
        return u;
    }

    public void logAuth(User u, String text) {
        Tx.run(c -> logDAO.insert(c, u.getUserId(), "AUTH", text));
    }

    public List<User> findAll() { return userDAO.findAll(); }
    public List<User> findActiveStaff() { return userDAO.findActiveStaff(); }
    public User findById(int id) { return userDAO.findById(id); }

    public void create(User u, String rawPassword, int actorId) {
        if (userDAO.emailExists(u.getEmail(), 0)) throw new BusinessException("That email address is already registered.");
        u.setPasswordHash(PasswordUtil.hash(rawPassword));
        Tx.run(c -> {
            userDAO.insert(c, u);
            logDAO.insert(c, actorId, "USER", "Created " + u.getRole().toLowerCase() + " account for " + u.getFullName());
        });
    }

    public void update(User u, int actorId) {
        User old = userDAO.findById(u.getUserId());
        if (old == null) throw new BusinessException("User not found.");
        if (userDAO.emailExists(u.getEmail(), u.getUserId())) throw new BusinessException("That email address is already registered.");
        guardLastAdmin(old, u.getRole(), u.getStatus());
        if (old.getUserId() == actorId && !"ACTIVE".equals(u.getStatus()))
            throw new BusinessException("You cannot deactivate your own account.");
        Tx.run(c -> {
            userDAO.update(c, u);
            logDAO.insert(c, actorId, "USER", "Updated user " + u.getFullName());
        });
    }

    public void setActive(int userId, boolean active, int actorId) {
        User old = userDAO.findById(userId);
        if (old == null) throw new BusinessException("User not found.");
        if (userId == actorId && !active) throw new BusinessException("You cannot deactivate your own account.");
        guardLastAdmin(old, old.getRole(), active ? "ACTIVE" : "INACTIVE");
        Tx.run(c -> {
            userDAO.setStatus(c, userId, active ? "ACTIVE" : "INACTIVE");
            logDAO.insert(c, actorId, "USER", (active ? "Activated " : "Deactivated ") + old.getFullName());
        });
    }

    public void resetPassword(int userId, String newPassword, int actorId) {
        User old = userDAO.findById(userId);
        if (old == null) throw new BusinessException("User not found.");
        String hash = PasswordUtil.hash(newPassword);
        Tx.run(c -> {
            userDAO.updatePassword(c, userId, hash);
            logDAO.insert(c, actorId, "USER", "Reset password for " + old.getFullName());
        });
    }

    public void updateProfile(int userId, String name, String phone) {
        Tx.run(c -> {
            userDAO.updateProfile(c, userId, name, phone);
            logDAO.insert(c, userId, "USER", "Updated own profile");
        });
    }

    public void changeOwnPassword(int userId, String current, String newPassword) {
        User u = userDAO.findById(userId);
        if (u == null || !PasswordUtil.verify(current, u.getPasswordHash()))
            throw new BusinessException("Current password is incorrect.");
        String hash = PasswordUtil.hash(newPassword);
        Tx.run(c -> {
            userDAO.updatePassword(c, userId, hash);
            logDAO.insert(c, userId, "USER", "Changed own password");
        });
    }

    /** Prevents locking everyone out: at least one active admin must always remain. */
    private void guardLastAdmin(User old, String newRole, String newStatus) {
        boolean wasActiveAdmin = old.isAdmin() && old.isActive();
        boolean stillActiveAdmin = "ADMIN".equals(newRole) && "ACTIVE".equals(newStatus);
        if (wasActiveAdmin && !stillActiveAdmin && userDAO.countActiveAdmins() <= 1)
            throw new BusinessException("At least one active administrator must remain.");
    }
}
