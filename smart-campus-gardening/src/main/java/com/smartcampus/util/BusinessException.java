package com.smartcampus.util;

/** A rule violation whose message is safe to show to the user (e.g. "Email already in use"). */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
