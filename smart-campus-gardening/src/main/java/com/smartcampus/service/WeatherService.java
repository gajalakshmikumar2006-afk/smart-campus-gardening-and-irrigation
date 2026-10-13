package com.smartcampus.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.smartcampus.dao.ActivityLogDAO;
import com.smartcampus.dao.SettingsDAO;
import com.smartcampus.dao.WeatherDAO;
import com.smartcampus.model.WeatherRecord;
import com.smartcampus.util.AppConfig;
import com.smartcampus.util.BusinessException;
import com.smartcampus.util.Tx;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Weather integration using Java's built-in HttpClient and the OpenWeather API.
 * If no API key is configured, DEMO MODE stores clearly labelled sample data (is_demo = 1).
 */
public class WeatherService {
    private static final String BASE = "https://api.openweathermap.org/data/2.5/";
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(6)).build();
    private final Gson gson = new Gson();
    private final WeatherDAO dao = new WeatherDAO();
    private final SettingsDAO settings = new SettingsDAO();
    private final ActivityLogDAO logDAO = new ActivityLogDAO();
    private final NotificationService notifications = new NotificationService();

    public String campusLocation() { return settings.get("campus_location", "Chennai,IN"); }
    public WeatherRecord latest() { return dao.findLatest(); }
    public boolean apiKeyConfigured() { return AppConfig.openWeatherApiKey() != null; }

    public void saveLocation(String location, int actorId) {
        Tx.run(c -> {
            settings.set(c, "campus_location", location);
            logDAO.insert(c, actorId, "WEATHER", "Campus weather location set to " + location);
        });
    }

    /** True when there is no record, it is older than 60 minutes, or the campus location changed. */
    public boolean isStale(WeatherRecord w) {
        return w == null || !w.getCampusLocation().equals(campusLocation())
                || w.getRetrievedAt().getTime() < System.currentTimeMillis() - 60L * 60 * 1000;
    }

    /** Fetches live weather (or demo data), stores it, and returns it. Throws BusinessException with a friendly fallback message. */
    public WeatherRecord refresh() {
        String location = campusLocation();
        String key = AppConfig.openWeatherApiKey();
        WeatherRecord w = (key == null) ? demo(location) : fetchLive(location, key);
        dao.insert(w);
        if (!w.isDemo() && w.getRainMmForecast24h() >= IrrigationService.RAIN_SKIP_MM) {
            try {
                Tx.run(c -> notifications.notifyAdmins(c, "Rain forecast (~" + Math.round(w.getRainMmForecast24h())
                        + " mm in 24h) for " + location + ". Consider postponing irrigation.", "WEATHER"));
            } catch (RuntimeException ignored) { /* weather data is saved; the alert is best-effort */ }
        }
        return dao.findLatest();
    }

    public List<Map<String, Object>> forecastOf(WeatherRecord w) {
        if (w == null || w.getForecastSummary() == null || w.getForecastSummary().isBlank()) return new ArrayList<>();
        try {
            return gson.fromJson(w.getForecastSummary(), new TypeToken<List<Map<String, Object>>>() { }.getType());
        } catch (RuntimeException e) { return new ArrayList<>(); }
    }

    // ------------------------------------------------------------------ live API
    private WeatherRecord fetchLive(String location, String key) {
        try {
            String q = URLEncoder.encode(location, StandardCharsets.UTF_8);
            JsonObject cur = getJson(BASE + "weather?q=" + q + "&units=metric&appid=" + key).getAsJsonObject();
            JsonObject fc = getJson(BASE + "forecast?q=" + q + "&units=metric&cnt=16&appid=" + key).getAsJsonObject();

            WeatherRecord w = new WeatherRecord();
            w.setCampusLocation(location);
            w.setTemperatureC(cur.getAsJsonObject("main").get("temp").getAsDouble());
            w.setHumidity(cur.getAsJsonObject("main").get("humidity").getAsInt());
            w.setConditions(capitalise(cur.getAsJsonArray("weather").get(0).getAsJsonObject().get("description").getAsString()));
            JsonObject rain = cur.has("rain") ? cur.getAsJsonObject("rain") : null;
            w.setRainMmCurrent(rain != null && rain.has("1h") ? rain.get("1h").getAsDouble() : 0);

            List<Map<String, Object>> entries = new ArrayList<>();
            double rain24 = 0;
            JsonArray list = fc.getAsJsonArray("list");
            for (int i = 0; i < list.size(); i++) {
                JsonObject e = list.get(i).getAsJsonObject();
                double mm = e.has("rain") && e.getAsJsonObject("rain").has("3h") ? e.getAsJsonObject("rain").get("3h").getAsDouble() : 0;
                if (i < 8) rain24 += mm;            // 8 x 3-hour slots = next 24 hours
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("time", e.get("dt_txt").getAsString().substring(5, 16));
                m.put("temp", Math.round(e.getAsJsonObject("main").get("temp").getAsDouble() * 10) / 10.0);
                m.put("conditions", capitalise(e.getAsJsonArray("weather").get(0).getAsJsonObject().get("description").getAsString()));
                m.put("rainMm", mm);
                JsonElement pop = e.get("pop");
                m.put("pop", pop == null ? 0 : Math.round(pop.getAsDouble() * 100));
                entries.add(m);
            }
            w.setRainMmForecast24h(Math.round(rain24 * 10) / 10.0);
            w.setForecastSummary(gson.toJson(entries));
            w.setDemo(false);
            return w;
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessException("The weather service could not be reached. Showing the last saved data instead (if any).");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("The weather request was interrupted. Please try again.");
        } catch (RuntimeException e) {
            throw new BusinessException("The weather service returned an unexpected response. Showing the last saved data instead (if any).");
        }
    }

    private JsonElement getJson(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(8)).GET().build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        int s = res.statusCode();
        if (s == 401) throw new BusinessException("The weather API key was rejected. Ask the administrator to check the key.");
        if (s == 404) throw new BusinessException("The campus location was not found by the weather service. Check the location setting (example: Chennai,IN).");
        if (s != 200) throw new BusinessException("The weather service is temporarily unavailable (HTTP " + s + "). Showing the last saved data instead (if any).");
        return JsonParser.parseString(res.body());
    }

    // ------------------------------------------------------------------ demo mode
    private WeatherRecord demo(String location) {
        WeatherRecord w = new WeatherRecord();
        w.setCampusLocation(location);
        w.setTemperatureC(31.0);
        w.setHumidity(68);
        w.setConditions("Partly cloudy (DEMO DATA)");
        w.setRainMmCurrent(0);
        w.setRainMmForecast24h(1.2);
        String[] cond = {"Sunny", "Partly cloudy", "Cloudy", "Light rain", "Cloudy", "Sunny", "Sunny", "Partly cloudy"};
        double[] rain = {0, 0, 0.4, 0.8, 0, 0, 0, 0};
        List<Map<String, Object>> entries = new ArrayList<>();
        for (int i = 0; i < cond.length; i++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("time", "Demo +" + ((i + 1) * 3) + "h");
            m.put("temp", 32 - i * 0.5);
            m.put("conditions", cond[i]);
            m.put("rainMm", rain[i]);
            m.put("pop", rain[i] > 0 ? 40 : 10);
            entries.add(m);
        }
        w.setForecastSummary(gson.toJson(entries));
        w.setDemo(true);
        return w;
    }

    private static String capitalise(String s) { return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1); }
}
