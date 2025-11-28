import dev.artha.annotations.Step;
import dev.artha.http.Request;
import com.google.gson.Gson;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

// ============================================
// TODO MODEL
// ===========================================
class Todo {
    private int id;
    private String title;
    private String description;
    private boolean completed;
    private String created_at;

    public Todo() {
    }

    public Todo(int id, String title, String description, boolean completed, String created_at) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.created_at = created_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}

// ============================================
// API ENDPOINTS
// ============================================

// GET all todos
@Step(path = "/api/todos", method = "GET")
class GetAllTodos {
    private static final Gson gson = new Gson();

    public String handle(Connection db) throws SQLException {
        try {
            initTable(db);

            List<Todo> todos = new ArrayList<>();
            String sql = "SELECT * FROM todos ORDER BY created_at DESC";

            try (Statement stmt = db.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    todos.add(new Todo(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getBoolean("completed"),
                            rs.getString("created_at")));
                }
            }
            return gson.toJson(todos);
        } finally {
            db.close();
        }
    }

    private void initTable(Connection db) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS todos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "completed BOOLEAN DEFAULT 0, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        try (Statement stmt = db.createStatement()) {
            stmt.execute(sql);
        }
    }
}

// GET single todo by ID
@Step(path = "/api/todos/{id}", method = "GET")
class GetTodoById {
    private static final Gson gson = new Gson();

    public String handle(Request request, Connection db) throws SQLException {
        try {
            String id = request.param("id");
            String sql = "SELECT * FROM todos WHERE id = ?";

            try (PreparedStatement pstmt = db.prepareStatement(sql)) {
                pstmt.setInt(1, Integer.parseInt(id));
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    return gson.toJson(new Todo(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getBoolean("completed"),
                            rs.getString("created_at")));
                }
                throw new IllegalArgumentException("Todo not found");
            }
        } finally {
            db.close();
        }
    }
}

// POST create new todo
@Step(path = "/api/todos", method = "POST")
class CreateTodo {
    private static final Gson gson = new Gson();

    public String handle(Todo todo, Connection db) throws SQLException {
        try {
            if (todo.getTitle() == null || todo.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Title is required");
            }

            String sql = "INSERT INTO todos (title, description, completed) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, todo.getTitle());
                pstmt.setString(2, todo.getDescription());
                pstmt.setBoolean(3, todo.isCompleted());
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    todo.setId(rs.getInt(1));
                }
            }
            return gson.toJson(todo);
        } finally {
            db.close();
        }
    }
}

// PUT update todo
@Step(path = "/api/todos/{id}", method = "PUT")
class UpdateTodo {
    private static final Gson gson = new Gson();

    public String handle(Request request, Todo todo, Connection db) throws SQLException {
        try {
            String id = request.param("id");
            String sql = "UPDATE todos SET title = ?, description = ?, completed = ? WHERE id = ?";

            try (PreparedStatement pstmt = db.prepareStatement(sql)) {
                pstmt.setString(1, todo.getTitle());
                pstmt.setString(2, todo.getDescription());
                pstmt.setBoolean(3, todo.isCompleted());
                pstmt.setInt(4, Integer.parseInt(id));

                if (pstmt.executeUpdate() > 0) {
                    todo.setId(Integer.parseInt(id));
                    return gson.toJson(todo);
                }
                throw new IllegalArgumentException("Todo not found");
            }
        } finally {
            db.close();
        }
    }
}

// DELETE todo
@Step(path = "/api/todos/{id}", method = "DELETE")
class DeleteTodo {
    public String handle(Request request, Connection db) throws SQLException {
        try {
            String id = request.param("id");
            String sql = "DELETE FROM todos WHERE id = ?";

            try (PreparedStatement pstmt = db.prepareStatement(sql)) {
                pstmt.setInt(1, Integer.parseInt(id));
                if (pstmt.executeUpdate() > 0) {
                    return "{\"message\": \"Todo deleted successfully\"}";
                }
                throw new IllegalArgumentException("Todo not found");
            }
        } finally {
            db.close();
        }
    }
}

// PATCH toggle todo completion
@Step(path = "/api/todos/{id}/toggle", method = "PATCH")
class ToggleTodo {
    private static final Gson gson = new Gson();

    public String handle(Request request, Connection db) throws SQLException {
        try {
            String id = request.param("id");

            String updateSql = "UPDATE todos SET completed = NOT completed WHERE id = ?";
            try (PreparedStatement pstmt = db.prepareStatement(updateSql)) {
                pstmt.setInt(1, Integer.parseInt(id));
                if (pstmt.executeUpdate() == 0) {
                    throw new IllegalArgumentException("Todo not found");
                }
            }

            String selectSql = "SELECT * FROM todos WHERE id = ?";
            try (PreparedStatement pstmt = db.prepareStatement(selectSql)) {
                pstmt.setInt(1, Integer.parseInt(id));
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return gson.toJson(new Todo(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getBoolean("completed"),
                            rs.getString("created_at")));
                }
            }
            throw new IllegalArgumentException("Todo not found");
        } finally {
            db.close();
        }
    }
}

// GET statistics
@Step(path = "/api/todos/stats", method = "GET")
class GetTodoStats {
    private static final Gson gson = new Gson();

    public String handle(Connection db) throws SQLException {
        try {
            Map<String, Object> stats = new HashMap<>();

            try (Statement stmt = db.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM todos");
                if (rs.next())
                    stats.put("total", rs.getInt("total"));
            }

            try (Statement stmt = db.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as completed FROM todos WHERE completed = 1");
                if (rs.next())
                    stats.put("completed", rs.getInt("completed"));
            }

            try (Statement stmt = db.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as pending FROM todos WHERE completed = 0");
                if (rs.next())
                    stats.put("pending", rs.getInt("pending"));
            }

            return gson.toJson(stats);
        } finally {
            db.close();
        }
    }
}

// DELETE all completed todos
@Step(path = "/api/todos/completed", method = "DELETE")
class DeleteCompletedTodos {
    private static final Gson gson = new Gson();

    public String handle(Connection db) throws SQLException {
        try {
            String sql = "DELETE FROM todos WHERE completed = 1";
            try (Statement stmt = db.createStatement()) {
                int deletedCount = stmt.executeUpdate(sql);
                Map<String, Object> result = new HashMap<>();
                result.put("message", "Completed todos deleted successfully");
                result.put("deletedCount", deletedCount);
                return gson.toJson(result);
            }
        } finally {
            db.close();
        }
    }
}
