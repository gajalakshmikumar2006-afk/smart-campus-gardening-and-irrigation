package com.smartcampus.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Loads secrets that must NOT live in source code (e.g. the OpenWeather API key).
 * Lookup order: environment variable OPENWEATHER_API_KEY,
 * then the file  ~/smartcampus-secrets.properties  (key: openweather.api.key).
 * If neither exists the app runs the Weather module in clearly labelled DEMO MODE.
 */
public final class AppConfig {
    private AppConfig() { }

    public static String openWeatherApiKey() {
        String env = System.getenv("OPENWEATHER_API_KEY");
        if (env != null && !env.isBlank()) return env.trim();
        Path file = Paths.get(System.getProperty("user.home"), "smartcampus-secrets.properties");
        if (Files.isReadable(file)) {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(file)) {
                p.load(in);
                String k = p.getProperty("openweather.api.key");
                if (k != null && !k.isBlank()) return k.trim();
            } catch (IOException ignored) { }
        }
        return null;
    }
}
