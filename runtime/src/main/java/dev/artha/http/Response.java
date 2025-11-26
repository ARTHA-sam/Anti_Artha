package dev.artha.http;

/**
 * ARTHA Response - Simplified HTTP response wrapper
 */
public interface Response {
    Response status(int code);
    Response json(Object obj);
    Response text(String text);
    Response header(String key, String value);
}
