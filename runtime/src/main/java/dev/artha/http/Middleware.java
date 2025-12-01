package dev.artha.http;

/**
 * Interface for middleware that can intercept requests before or after the main
 * handler.
 */
public interface Middleware {
    /**
     * Apply the middleware logic.
     * 
     * @param req The request object
     * @param res The response object
     * @throws Exception If the middleware fails or wants to halt processing
     */
    void apply(Request req, Response res) throws Exception;
}
