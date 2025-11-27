package todo;

import dev.artha.annotations.Step;
import dev.artha.http.Request;
import dev.artha.http.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TodoApp {

    // In-memory database
    private static final List<Todo> todos = new ArrayList<>();
    private static final AtomicInteger idCounter = new AtomicInteger(1);

    // Pre-populate with some data
    static {
        todos.add(new Todo(idCounter.getAndIncrement(), "Learn ARTHA Framework", true));
        todos.add(new Todo(idCounter.getAndIncrement(), "Build a To-Do App", false));
        todos.add(new Todo(idCounter.getAndIncrement(), "Star the repo", false));
    }

    @Step(path = "/", method = "GET")
    public Object home(Request req, Response res) {
        return Map.of(
                "message", "Welcome to the ARTHA To-Do API!",
                "endpoints", List.of(
                        "GET /todos - List all todos",
                        "POST /todos - Create a new todo",
                        "PATCH /todos/:id - Update a todo",
                        "DELETE /todos/:id - Delete a todo"));
    }

    @Step(path = "/todos", method = "GET")
    public List<Todo> getAll(Request req, Response res) {
        return todos;
    }

    @Step(path = "/todos", method = "POST")
    public Object create(Request req, Response res) {
        try {
            Todo newTodo = req.body(Todo.class);
            if (newTodo.getTitle() == null || newTodo.getTitle().isEmpty()) {
                res.status(400);
                return Map.of("error", "Title is required");
            }

            newTodo.setId(idCounter.getAndIncrement());
            todos.add(newTodo);

            res.status(201);
            return newTodo;
        } catch (Exception e) {
            res.status(400);
            return Map.of("error", "Invalid JSON body");
        }
    }

    @Step(path = "/todos/:id", method = "PATCH")
    public Object update(Request req, Response res) {
        int id = Integer.parseInt(req.param("id"));

        Todo todo = todos.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);

        if (todo == null) {
            res.status(404);
            return Map.of("error", "Todo not found");
        }

        try {
            // We can use a Map for partial updates since we don't have a full Todo object
            // Or just use the Todo class and check for nulls (simple approach)
            Todo updates = req.body(Todo.class);

            if (updates.getTitle() != null) {
                todo.setTitle(updates.getTitle());
            }
            // Boolean fields are tricky with standard POJOs (isCompleted vs getCompleted),
            // but for this simple example we'll assume if it's sent, we update it.
            // A better way is using a Map<String, Object> for PATCH.

            // Let's try parsing as Map for better PATCH support
            Map<String, Object> updateMap = req.body(Map.class);
            if (updateMap.containsKey("title")) {
                todo.setTitle((String) updateMap.get("title"));
            }
            if (updateMap.containsKey("completed")) {
                todo.setCompleted((Boolean) updateMap.get("completed"));
            }

            return todo;
        } catch (Exception e) {
            res.status(400);
            return Map.of("error", "Invalid JSON body");
        }
    }

    @Step(path = "/todos/:id", method = "DELETE")
    public Object delete(Request req, Response res) {
        int id = Integer.parseInt(req.param("id"));

        boolean removed = todos.removeIf(t -> t.getId() == id);

        if (removed) {
            return Map.of("success", true, "message", "Todo deleted");
        } else {
            res.status(404);
            return Map.of("error", "Todo not found");
        }
    }
}
