import dev.artha.http.*;

public class AuthMiddleware implements Middleware {

    @Override
    public void apply(Request req, Response res) throws Exception {
        String auth = req.header("Authorization");

        if (auth == null || !auth.startsWith("Bearer ")) {
            res.status(401);
            throw new Exception("Unauthorized");
        }

        String token = auth.substring(7);
        if (!token.contains("user_") || !token.endsWith("_token")) {
            res.status(401);
            throw new Exception("Invalid token");
        }
    }
}
