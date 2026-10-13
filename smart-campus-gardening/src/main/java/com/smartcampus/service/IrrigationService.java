package com.smartcampus.service;

import com.smartcampus.dao.ActivityLogDAO;
import com.smartcampus.dao.GardenDAO;
import com.smartcampus.dao.IrrigationDAO;
import com.smartcampus.dao.PlantDAO;
import com.smartcampus.dao.UserDAO;
import com.smartcampus.dao.WeatherDAO;
import com.smartcampus.model.Garden;
import com.smartcampus.model.IrrigationRecord;
import com.smartcampus.model.IrrigationSchedule;
import com.smartcampus.model.Plant;
import com.smartcampus.model.Recommendation;
import com.smartcampus.model.User;
import com.smartcampus.model.WeatherRecord;
import com.smartcampus.util.BusinessException;
import com.smartcampus.util.Tx;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Irrigation business logic. NOTE: this is a software scheduling/record-keeping tool.
 * It does not control any physical valves or pumps, and water volumes are ESTIMATES.
 */
public class IrrigationService {
    static final double RAIN_SKIP_MM = 5.0;      // >= this much expected rain => postpone
    static final double RAIN_REDUCE_MM = 2.0;    // >= this much => halve the quantity
    static final double HOT_TEMP_C = 35.0;       // hot day => add 10 %
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    private final IrrigationDAO dao = new IrrigationDAO();
    private final GardenDAO gardenDAO = new GardenDAO();
    private final PlantDAO plantDAO = new PlantDAO();
    private final UserDAO userDAO = new UserDAO();
    private final WeatherDAO weatherDAO = new WeatherDAO();
    private final ActivityLogDAO logDAO = new ActivityLogDAO();
    private final NotificationService notifications = new NotificationService();

    public List<IrrigationSchedule> upcoming(User u) { return dao.findUpcoming(u.isAdmin() ? null : u.getUserId()); }
    public List<IrrigationSchedule> history(User u) { return dao.findHistory(u.isAdmin() ? null : u.getUserId(), 50); }
    public List<IrrigationSchedule> dueSoon(User u, int hours) { return dao.findDueSoon(u.isAdmin() ? null : u.getUserId(), hours); }

    // ------------------------------------------------------------------ create / edit / cancel
    public void create(IrrigationSchedule s, User actor) {
        s.setStatus("PENDING");
        validateTargets(s);
        if (s.getScheduledAt().before(new Timestamp(System.currentTimeMillis() - 60_000)))
            throw new BusinessException("Schedule date and time must be in the future.");
        if (dao.openScheduleExists(s.getGardenId(), s.getScheduledAt(), 0))
            throw new BusinessException("An open irrigation schedule already exists for this garden on that day.");
        Garden g = gardenDAO.findById(s.getGardenId());
        Tx.run(c -> {
            dao.insert(c, s, actor.getUserId());
            notifications.notifyUser(c, s.getAssignedUserId(),
                "New irrigation assigned: " + g.getGardenName() + " on " + FMT.format(s.getScheduledAt().toLocalDateTime()), "IRRIGATION");
            logDAO.insert(c, actor.getUserId(), "IRRIGATION", "Created irrigation schedule for " + g.getGardenName());
        });
    }

    public void update(IrrigationSchedule s, User actor) {
        IrrigationSchedule old = dao.findById(s.getScheduleId());
        if (old == null) throw new BusinessException("Schedule not found.");
        if (!old.isActionable()) throw new BusinessException("Completed or cancelled schedules cannot be edited.");
        validateTargets(s);
        if (dao.openScheduleExists(s.getGardenId(), s.getScheduledAt(), s.getScheduleId()))
            throw new BusinessException("Another open schedule already exists for this garden on that day.");
        boolean timeChanged = !old.getScheduledAt().equals(s.getScheduledAt());
        if (timeChanged && "PENDING".equals(s.getStatus())) s.setStatus("RESCHEDULED");
        if ("COMPLETED".equals(s.getStatus())) throw new BusinessException("Use 'Mark completed' to record completion.");
        Garden g = gardenDAO.findById(s.getGardenId());
        Tx.run(c -> {
            dao.update(c, s);
            String msg = "Irrigation for " + g.getGardenName() + " " + (timeChanged ? "was rescheduled to " + FMT.format(s.getScheduledAt().toLocalDateTime()) : "was updated");
            notifications.notifyUser(c, s.getAssignedUserId(), msg, "IRRIGATION");
            if (old.getAssignedUserId() != null && !old.getAssignedUserId().equals(s.getAssignedUserId()))
                notifications.notifyUser(c, old.getAssignedUserId(), "You were unassigned from irrigation of " + g.getGardenName(), "IRRIGATION");
            logDAO.insert(c, actor.getUserId(), "IRRIGATION", "Updated irrigation schedule #" + s.getScheduleId() + " (" + g.getGardenName() + ")");
        });
    }

    public void cancel(int id, User actor) {
        IrrigationSchedule old = dao.findById(id);
        if (old == null) throw new BusinessException("Schedule not found.");
        if (!old.isActionable()) throw new BusinessException("Only pending or rescheduled schedules can be cancelled.");
        Tx.run(c -> {
            dao.updateStatus(c, id, "CANCELLED");
            notifications.notifyUser(c, old.getAssignedUserId(), "Irrigation for " + old.getGardenName() + " was cancelled.", "IRRIGATION");
            logDAO.insert(c, actor.getUserId(), "IRRIGATION", "Cancelled irrigation schedule #" + id + " (" + old.getGardenName() + ")");
        });
    }

    // ------------------------------------------------------------------ status changes (staff or admin)
    /** Admin may change any schedule; staff only schedules assigned to them. */
    public void changeStatus(int id, String status, Double litersUsed, String notes, User actor) {
        IrrigationSchedule s = dao.findById(id);
        if (s == null) throw new BusinessException("Schedule not found.");
        if (!actor.isAdmin() && (s.getAssignedUserId() == null || s.getAssignedUserId() != actor.getUserId()))
            throw new BusinessException("You can only update schedules assigned to you.");
        if (!s.isActionable()) throw new BusinessException("This schedule is already " + s.getStatus().toLowerCase() + ".");
        if ("COMPLETED".equals(status)) { complete(s, litersUsed, notes, actor); return; }
        if (!"PENDING".equals(status) && !"RESCHEDULED".equals(status)) throw new BusinessException("Invalid status.");
        Tx.run(c -> {
            dao.updateStatus(c, id, status);
            notifications.notifyAdmins(c, actor.getFullName() + " marked irrigation of " + s.getGardenName() + " as " + status.toLowerCase(), "IRRIGATION");
            logDAO.insert(c, actor.getUserId(), "IRRIGATION", "Irrigation #" + id + " (" + s.getGardenName() + ") set to " + status);
        });
    }

    /** One transaction: mark completed + write irrigation record (water usage) + notify admins + activity log. */
    private void complete(IrrigationSchedule s, Double litersUsed, String notes, User actor) {
        double used = (litersUsed != null && litersUsed > 0) ? litersUsed : s.getEstimatedWaterLiters();
        IrrigationRecord r = new IrrigationRecord();
        r.setScheduleId(s.getScheduleId());
        r.setCompletedAt(new Timestamp(System.currentTimeMillis()));
        r.setEstimatedWaterUsedLiters(used);
        r.setNotes(notes);
        Tx.run(c -> {
            dao.updateStatus(c, s.getScheduleId(), "COMPLETED");
            dao.insertRecord(c, r, actor.getUserId());
            notifications.notifyAdmins(c, s.getGardenName() + " irrigation completed by " + actor.getFullName()
                    + " (estimated " + String.format(Locale.ENGLISH, "%.1f", used) + " L)", "IRRIGATION");
            logDAO.insert(c, actor.getUserId(), "IRRIGATION", "Completed irrigation for " + s.getGardenName()
                    + " (estimated " + String.format(Locale.ENGLISH, "%.1f", used) + " L)");
        });
    }

    private void validateTargets(IrrigationSchedule s) {
        Garden g = gardenDAO.findById(s.getGardenId());
        if (g == null) throw new BusinessException("Please choose a valid garden.");
        if ("INACTIVE".equals(g.getStatus())) throw new BusinessException("Cannot schedule irrigation for an inactive garden.");
        if (s.getAssignedUserId() != null) {
            User u = userDAO.findById(s.getAssignedUserId());
            if (u == null || !u.isActive() || u.isAdmin()) throw new BusinessException("Please assign an active maintenance staff member.");
        }
    }

    // ------------------------------------------------------------------ recommendation engine
    public List<Recommendation> recommendAll() {
        List<Recommendation> list = new ArrayList<>();
        for (Garden g : gardenDAO.findAll()) if (!"INACTIVE".equals(g.getStatus())) list.add(recommend(g.getGardenId()));
        return list;
    }

    /**
     * Rule-based recommendation using (1) plant needs, (2) previous irrigation records, (3) latest weather record.
     * Quantity = sum(plant quantity x litres per plant). Watering is due when days since last irrigation
     * >= the SHORTEST watering interval among the garden's plants.
     */
    public Recommendation recommend(int gardenId) {
        Garden g = gardenDAO.findById(gardenId);
        if (g == null) throw new BusinessException("Garden not found.");
        List<String> why = new ArrayList<>();
        List<Plant> plants = plantDAO.findByGarden(gardenId);
        if (plants.isEmpty()) {
            why.add("No plants are recorded for this garden, so a water quantity cannot be calculated. Add plants first.");
            return new Recommendation(gardenId, g.getGardenName(), false, 0, "Cannot recommend - no plants recorded", why, false);
        }
        double liters = 0;
        int minFreq = Integer.MAX_VALUE;
        for (Plant p : plants) {
            liters += p.getQuantity() * p.getWaterRequirementLiters();
            minFreq = Math.min(minFreq, Math.max(1, p.getWateringFrequencyDays()));
        }
        why.add("Plant needs: " + plants.size() + " plant group(s) need about " + fmt(liters) + " L per watering; the most frequent need is every " + minFreq + " day(s).");

        Timestamp last = dao.lastCompletedAt(gardenId);
        boolean due;
        if (last == null) {
            due = true;
            why.add("No previous irrigation is recorded for this garden.");
        } else {
            long days = ChronoUnit.DAYS.between(last.toLocalDateTime().toLocalDate(), LocalDate.now());
            due = days >= minFreq;
            why.add("Last irrigation was " + days + " day(s) ago (" + FMT.format(last.toLocalDateTime()) + "); " +
                    (due ? "this is at or beyond the " + minFreq + "-day interval." : "the next watering is not due until " + minFreq + " day(s) have passed."));
        }

        boolean rainSkip = false, demo = false;
        WeatherRecord w = weatherDAO.findLatest();
        if (w == null) {
            why.add("No weather data is available yet, so this recommendation uses plant needs and irrigation history only.");
        } else {
            demo = w.isDemo();
            String src = demo ? "DEMO weather (not live)" : "Weather (" + w.getCampusLocation() + ")";
            double rain = w.getRainMmCurrent() + w.getRainMmForecast24h();
            if (rain >= RAIN_SKIP_MM) {
                rainSkip = true;
                why.add(src + ": about " + fmt(rain) + " mm of rain is expected in the next 24 hours (threshold " + fmt(RAIN_SKIP_MM) + " mm) - natural rainfall should be sufficient, so postpone.");
            } else if (rain >= RAIN_REDUCE_MM) {
                liters *= 0.5;
                why.add(src + ": light rain (about " + fmt(rain) + " mm) is expected, so the water quantity is reduced by half.");
            } else {
                why.add(src + ": little or no rain expected (" + fmt(rain) + " mm).");
            }
            if (!rainSkip && w.getTemperatureC() >= HOT_TEMP_C) {
                liters *= 1.10;
                why.add(src + ": it is hot (" + fmt(w.getTemperatureC()) + " C), so 10% extra water is suggested.");
            }
        }
        liters = Math.round(liters * 10.0) / 10.0;
        boolean waterNow = due && !rainSkip;
        String headline = waterNow ? "Watering recommended"
                : rainSkip ? "Postpone watering - rain expected" : "Not due yet - postpone watering";
        return new Recommendation(gardenId, g.getGardenName(), waterNow, liters, headline, why, demo);
    }

    private static String fmt(double v) { return String.format(Locale.ENGLISH, "%.1f", v); }
}
