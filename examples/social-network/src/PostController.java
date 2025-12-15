import dev.artha.annotations.*;
import dev.artha.http.*;
import dev.artha.db.Database;
import java.util.*;

@Step(path = "/api/posts")
@Before({ AuthMiddleware.class })
public class PostController {

    @Inject
    private Database db;

    @ConfigValue("app.maxPostLength")
    private int maxPostLength;

    @Step(path = "", method = "POST")
    public Object createPost(Request req, Response res) {
        try {
            int userId = getCurrentUserId(req);
            Map<String, Object> body = req.bodyAsMap();
            String content = (String) body.get("content");

            if (content == null || content.trim().isEmpty()) {
                res.status(400);
                return Map.of("error", "Content required");
            }

            if (content.length() > maxPostLength) {
                res.status(400);
                return Map.of("error", "Content too long");
            }

            int postId = db.table("posts").insert(Map.of(
                    "user_id", userId,
                    "content", content));

            res.status(201);
            return Map.of("postId", postId, "message", "Post created");

        } catch (Exception e) {
            res.status(500);
            return Map.of("error", e.getMessage());
        }
    }

    @Step(path = "/feed", method = "GET")
    public Object getFeed(Request req) {
        try {
            int userId = getCurrentUserId(req);

            List<Map<String, Object>> posts = db.execute(
                    "SELECT p.*, u.username, u.full_name " +
                            "FROM posts p " +
                            "JOIN users u ON p.user_id = u.id " +
                            "WHERE p.user_id IN (" +
                            "  SELECT following_id FROM follows WHERE follower_id = ?" +
                            ") OR p.user_id = ? " +
                            "ORDER BY p.created_at DESC LIMIT 20",
                    userId, userId);

            return Map.of("posts", posts);

        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @Step(path = "/{id}/like", method = "POST")
    public Object likePost(Request req, Response res) {
        try {
            int userId = getCurrentUserId(req);
            int postId = Integer.parseInt(req.param("id"));

            // Check if already liked
            List<Map<String, Object>> existing = db.execute(
                    "SELECT id FROM likes WHERE user_id = ? AND post_id = ?",
                    userId, postId);

            if (!existing.isEmpty()) {
                return Map.of("message", "Already liked");
            }

            db.table("likes").insert(Map.of("user_id", userId, "post_id", postId));
            db.execute("UPDATE posts SET likes_count = likes_count + 1 WHERE id = ?", postId);

            return Map.of("message", "Post liked");

        } catch (Exception e) {
            res.status(500);
            return Map.of("error", e.getMessage());
        }
    }

    private int getCurrentUserId(Request req) {
        String token = req.header("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String[] parts = token.split("_");
        return Integer.parseInt(parts[1]);
    }

    @ExceptionHandler(Exception.class)
    public Object handleError(Exception e, Response res) {
        res.status(500);
        return Map.of("error", e.getMessage());
    }
}
