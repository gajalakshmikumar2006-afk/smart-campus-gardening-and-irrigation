package com.smartcampus.service;

import com.smartcampus.dao.ActivityLogDAO;
import com.smartcampus.dao.GardenDAO;
import com.smartcampus.dao.MaintenanceDAO;
import com.smartcampus.dao.UserDAO;
import com.smartcampus.model.MaintenanceTask;
import com.smartcampus.model.User;
import com.smartcampus.util.BusinessException;
import com.smartcampus.util.Tx;
import java.util.List;
import java.util.Set;

public class MaintenanceService {
    private static final Set<String> STATUSES = Set.of("PENDING", "IN_PROGRESS", "COMPLETED");
    private final MaintenanceDAO dao = new MaintenanceDAO();
    private final GardenDAO gardenDAO = new GardenDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ActivityLogDAO logDAO = new ActivityLogDAO();
    private final NotificationService notifications = new NotificationService();

    /** Admin sees all tasks; staff see only tasks assigned to them. */
    public List<MaintenanceTask> list(User viewer, String status) {
        return dao.findAll(viewer.isAdmin() ? null : viewer.getUserId(), status);
    }

    public void create(MaintenanceTask t, User actor) {
        validate(t);
        t.setStatus("PENDING");
        Tx.run(c -> {
            dao.insert(c, t, actor.getUserId());
            notifications.notifyUser(c, t.getAssignedUserId(), "You have been assigned the task \"" + t.getTitle() + "\" (due " + t.getDueDate() + ").", "MAINTENANCE");
            logDAO.insert(c, actor.getUserId(), "MAINTENANCE", "Created task \"" + t.getTitle() + "\"");
        });
    }

    public void update(MaintenanceTask t, User actor) {
        MaintenanceTask old = dao.findById(t.getTaskId());
        if (old == null) throw new BusinessException("Task not found.");
        validate(t);
        if (!STATUSES.contains(t.getStatus())) throw new BusinessException("Invalid status.");
        Tx.run(c -> {
            dao.update(c, t);
            boolean reassigned = t.getAssignedUserId() != null && !t.getAssignedUserId().equals(old.getAssignedUserId());
            if (reassigned) notifications.notifyUser(c, t.getAssignedUserId(), "You have been assigned the task \"" + t.getTitle() + "\" (due " + t.getDueDate() + ").", "MAINTENANCE");
            logDAO.insert(c, actor.getUserId(), "MAINTENANCE", "Updated task \"" + t.getTitle() + "\"");
        });
    }

    public void delete(int id, User actor) {
        MaintenanceTask old = dao.findById(id);
        if (old == null) throw new BusinessException("Task not found.");
        Tx.run(c -> {
            dao.delete(c, id);
            logDAO.insert(c, actor.getUserId(), "MAINTENANCE", "Deleted task \"" + old.getTitle() + "\"");
        });
    }

    /** Admin: any task. Staff: only their own. */
    public void changeStatus(int id, String status, User actor) {
        if (!STATUSES.contains(status)) throw new BusinessException("Invalid status.");
        MaintenanceTask t = dao.findById(id);
        if (t == null) throw new BusinessException("Task not found.");
        if (!actor.isAdmin() && (t.getAssignedUserId() == null || t.getAssignedUserId() != actor.getUserId()))
            throw new BusinessException("You can only update tasks assigned to you.");
        Tx.run(c -> {
            dao.updateStatus(c, id, status);
            if (!actor.isAdmin() || "COMPLETED".equals(status))
                notifications.notifyAdmins(c, actor.getFullName() + " set task \"" + t.getTitle() + "\" to " + status.replace('_', ' ').toLowerCase(), "MAINTENANCE");
            logDAO.insert(c, actor.getUserId(), "MAINTENANCE", "Task \"" + t.getTitle() + "\" set to " + status);
        });
    }

    private void validate(MaintenanceTask t) {
        if (gardenDAO.findById(t.getGardenId()) == null) throw new BusinessException("Please choose a valid garden.");
        if (t.getAssignedUserId() != null) {
            User u = userDAO.findById(t.getAssignedUserId());
            if (u == null || !u.isActive() || u.isAdmin()) throw new BusinessException("Please assign an active maintenance staff member.");
        }
    }
}
