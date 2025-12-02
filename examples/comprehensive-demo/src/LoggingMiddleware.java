package demo;

import dev.artha.http.Middleware;
import dev.artha.http.Request;
import dev.artha.http.Response;

public class LoggingMiddleware implements Middleware {
    @Override
    public void apply(Request req, Response res) {
        System.out.println("📝 [LOG] " + req.method() + " " + req.path());
    }
}
