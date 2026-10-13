package com.smartcampus.service;

import com.smartcampus.dao.ActivityLogDAO;
import com.smartcampus.dao.GardenDAO;
import com.smartcampus.dao.PlantDAO;
import com.smartcampus.model.Plant;
import com.smartcampus.util.BusinessException;
import com.smartcampus.util.Tx;
import java.util.List;

public class PlantService {
    private final PlantDAO dao = new PlantDAO();
    private final GardenDAO gardenDAO = new GardenDAO();
    private final ActivityLogDAO logDAO = new ActivityLogDAO();

    public List<Plant> findAll(int gardenId, String category) { return dao.findAll(gardenId, category); }
    public List<String> categories() { return dao.findCategories(); }

    public void create(Plant p, int actorId) {
        requireGarden(p.getGardenId());
        Tx.run(c -> {
            dao.insert(c, p);
            logDAO.insert(c, actorId, "PLANT", "Added " + p.getQuantity() + " x " + p.getPlantName() + " to garden #" + p.getGardenId());
        });
    }

    public void update(Plant p, int actorId) {
        if (dao.findById(p.getPlantId()) == null) throw new BusinessException("Plant record not found.");
        requireGarden(p.getGardenId());
        Tx.run(c -> {
            dao.update(c, p);
            logDAO.insert(c, actorId, "PLANT", "Updated plant \"" + p.getPlantName() + "\"");
        });
    }

    public void delete(int id, int actorId) {
        Plant p = dao.findById(id);
        if (p == null) throw new BusinessException("Plant record not found.");
        Tx.run(c -> {
            dao.delete(c, id);
            logDAO.insert(c, actorId, "PLANT", "Deleted plant \"" + p.getPlantName() + "\"");
        });
    }

    private void requireGarden(int gardenId) {
        if (gardenDAO.findById(gardenId) == null) throw new BusinessException("Please choose a valid garden.");
    }
}
