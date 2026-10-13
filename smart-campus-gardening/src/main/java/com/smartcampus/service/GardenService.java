package com.smartcampus.service;

import com.smartcampus.dao.ActivityLogDAO;
import com.smartcampus.dao.GardenDAO;
import com.smartcampus.model.Garden;
import com.smartcampus.util.BusinessException;
import com.smartcampus.util.Tx;
import java.util.List;

public class GardenService {
    private final GardenDAO dao = new GardenDAO();
    private final ActivityLogDAO logDAO = new ActivityLogDAO();

    public List<Garden> findAll() { return dao.findAll(); }
    public Garden findById(int id) { return dao.findById(id); }

    public void create(Garden g, int actorId) {
        Tx.run(c -> {
            dao.insert(c, g);
            logDAO.insert(c, actorId, "GARDEN", "Added garden \"" + g.getGardenName() + "\"");
        });
    }

    public void update(Garden g, int actorId) {
        if (dao.findById(g.getGardenId()) == null) throw new BusinessException("Garden not found.");
        Tx.run(c -> {
            dao.update(c, g);
            logDAO.insert(c, actorId, "GARDEN", "Updated garden \"" + g.getGardenName() + "\"");
        });
    }

    /** Plants, schedules and tasks of the garden are removed by ON DELETE CASCADE. */
    public void delete(int id, int actorId) {
        Garden g = dao.findById(id);
        if (g == null) throw new BusinessException("Garden not found.");
        Tx.run(c -> {
            dao.delete(c, id);
            logDAO.insert(c, actorId, "GARDEN", "Deleted garden \"" + g.getGardenName() + "\" and its related records");
        });
    }
}
