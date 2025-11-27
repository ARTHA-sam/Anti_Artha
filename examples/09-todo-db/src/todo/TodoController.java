package todo;

import dev.artha.annotations.Step;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Step(path = "/todos", method = "GET")
public class TodoController {

    // Automatic Connection Injection!
    public List<Todo> handle(Connection db) throws Exception {
        // Create table if not exists
        try (Statement stmt = db.createStatement()) {
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS todos (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, completed BOOLEAN)");
        }

        // Insert a sample todo if empty
        try (Statement stmt = db.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM todos");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO todos (title, completed) VALUES ('Learn ARTHA', 1)");
                stmt.execute("INSERT INTO todos (title, completed) VALUES ('Build a DB App', 0)");
            }
        }

        // Fetch todos
        List<Todo> todos = new ArrayList<>();
        try (Statement stmt = db.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM todos")) {

            while (rs.next()) {
                todos.add(new Todo(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getBoolean("completed")));
            }
        }
        return todos;
    }
}
