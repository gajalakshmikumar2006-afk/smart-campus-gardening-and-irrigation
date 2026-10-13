package com.smartcampus.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/** Server-side input validation helpers. Each method adds a message to the error list when invalid. */
public final class Validator {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_RULE = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,64}$");
    private Validator() { }

    public static String trim(String s) { return s == null ? "" : s.trim(); }

    public static String text(String value, String label, int min, int max, List<String> errors) {
        String v = trim(value);
        if (v.length() < min) errors.add(label + (min == 1 ? " is required." : " must be at least " + min + " characters."));
        else if (v.length() > max) errors.add(label + " must be at most " + max + " characters.");
        return v;
    }

    public static String optionalText(String value, String label, int max, List<String> errors) {
        String v = trim(value);
        if (v.length() > max) errors.add(label + " must be at most " + max + " characters.");
        return v.isEmpty() ? null : v;
    }

    public static String email(String value, List<String> errors) {
        String v = trim(value).toLowerCase();
        if (!EMAIL.matcher(v).matches() || v.length() > 120) errors.add("Enter a valid email address.");
        return v;
    }

    public static boolean passwordOk(String pw, List<String> errors) {
        if (pw == null || !PASSWORD_RULE.matcher(pw).matches()) {
            errors.add("Password must be 8-64 characters and contain at least one letter and one digit.");
            return false;
        }
        return true;
    }

    public static String oneOf(String value, Set<String> allowed, String label, List<String> errors) {
        String v = trim(value).toUpperCase();
        if (!allowed.contains(v)) { errors.add("Invalid " + label + "."); }
        return v;
    }

    public static int integer(String value, String label, int min, int max, List<String> errors) {
        try {
            int n = Integer.parseInt(trim(value));
            if (n < min || n > max) errors.add(label + " must be between " + min + " and " + max + ".");
            return n;
        } catch (NumberFormatException e) {
            errors.add(label + " must be a whole number.");
            return 0;
        }
    }

    public static double decimal(String value, String label, double min, double max, List<String> errors) {
        try {
            double n = Double.parseDouble(trim(value));
            if (Double.isNaN(n) || n < min || n > max) errors.add(label + " must be between " + min + " and " + max + ".");
            return n;
        } catch (NumberFormatException e) {
            errors.add(label + " must be a number.");
            return 0;
        }
    }

    public static Date date(String value, String label, List<String> errors) {
        try { return Date.valueOf(LocalDate.parse(trim(value))); }
        catch (DateTimeParseException e) { errors.add(label + " must be a valid date."); return null; }
    }

    /** Accepts separate date (yyyy-MM-dd) and time (HH:mm) inputs. */
    public static Timestamp dateTime(String date, String time, String label, List<String> errors) {
        try { return Timestamp.valueOf(LocalDateTime.parse(trim(date) + "T" + trim(time))); }
        catch (DateTimeParseException e) { errors.add(label + " must have a valid date and time."); return null; }
    }

    /** Optional filter date: returns null when blank or invalid (never throws). */
    public static LocalDate optionalDate(String value) {
        try { return trim(value).isEmpty() ? null : LocalDate.parse(trim(value)); }
        catch (DateTimeParseException e) { return null; }
    }

    public static int intOrZero(String value) {
        try { return Integer.parseInt(trim(value)); } catch (NumberFormatException e) { return 0; }
    }
}
