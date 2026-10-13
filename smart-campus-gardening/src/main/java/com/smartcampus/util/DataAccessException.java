package com.smartcampus.util;

/** Unchecked wrapper for SQL errors so DAO signatures stay simple. Details are logged, never shown to users. */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) { super(message, cause); }
}
