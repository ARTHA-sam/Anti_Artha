package dev.artha.examples;

import dev.artha.annotations.Step;
import dev.artha.annotations.Before;
import dev.artha.annotations.After;
import dev.artha.http.Middleware;
import dev.artha.http.Request;
import dev.artha.http.Response;

// Middleware to check for "secret" query param
class AuthMiddleware implements Middleware {
    @Override
    public void apply(Request req, Response res) throws Exception {
        String secret = req.query("secret");
        if (!"open-sesame".equals(secret)) {
            res.status(401);
            res.json(java.util.Map.of("error", "Unauthorized"));
            throw new RuntimeException("Unauthorized access attempt");
        }
    }
}

// Middleware to log requests
class LoggingMiddleware implements Middleware {
    @Override
    public void apply(Request req, Response res) {
        System.out.println("LOG: " + req.method() + " " + req.url());
    }
}

@Step(path = "/secure")
@Before({ LoggingMiddleware.class })
public class MiddlewareDemo {

    @Step(path = "/data")
    @Before({ AuthMiddleware.class })
    public String getData() {
        return "This is secured data!";
    }

    @Step(path = "/public")
    public String getPublic() {
        return "This is public data";
    }
}
