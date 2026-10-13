package com.smartcampus.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Reusable database connection factory.
 * Settings come from db.properties (classpath) and can be overridden with the
 * environment variables DB_URL, DB_USER and DB_PASSWORD.
 */
public final class DBUtil {
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties p = new Properties();
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) p.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Cannot read db.properties: " + e.getMessage());
        }
        URL = env("DB_URL", p.getProperty("db.url"));
        USER = env("DB_USER", p.getProperty("db.user"));
        PASSWORD = env("DB_PASSWORD", p.getProperty("db.password", ""));
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL JDBC driver not found on classpath");
        }
    }

    private DBUtil() { }

    private static String env(String key, String fallback) {
        String v = System.getenv(key);
        return (v != null && !v.isBlank()) ? v : fallback;
    }

    /** Opens a new connection. Always use inside try-with-resources. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
