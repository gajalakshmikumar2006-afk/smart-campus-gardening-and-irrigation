package com.smartcampus.service;

import com.smartcampus.dao.NotificationDAO;
import com.smartcampus.dao.UserDAO;
import com.smartcampus.model.Notification;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class NotificationService {
    private final NotificationDAO dao = new NotificationDAO();
    private final UserDAO userDAO = new UserDAO();

    /** Joins the caller's transaction so the notification is saved only if the main change is saved. */
    public void notifyUser(Connection c, Integer userId, String message, String type) throws SQLException {
        if (userId != null) dao.insert(c, userId, message, type);
    }

    public void notifyAdmins(Connection c, String message, String type) throws SQLException {
        for (int id : userDAO.findActiveAdminIds()) dao.insert(c, id, message, type);
    }

    public List<Notification> list(int userId) { return dao.findByUser(userId, 100); }
    public int unreadCount(int userId) { return dao.countUnread(userId); }
    public void markRead(int id, int userId) { dao.markRead(id, userId); }
    public void markAllRead(int userId) { dao.markAllRead(userId); }
}
