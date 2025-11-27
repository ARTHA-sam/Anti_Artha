package todo;

import java.util.Objects;

public class Todo {
    private int id;
    private String title;
    private boolean completed;

    // No-args constructor
    public Todo() {
    }

    // All-args constructor
    public Todo(int id, String title, boolean completed) {
        this.id = id;
        this.title = title;
        this.completed = completed;
    }

    // Getters and Setters
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

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    // toString
    @Override
    public String toString() {
        return "Todo{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", completed=" + completed +
                '}';
    }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Todo))
            return false;
        Todo todo = (Todo) o;
        return id == todo.id &&
                completed == todo.completed &&
                Objects.equals(title, todo.title);
    }

    // hashCode
    @Override
    public int hashCode() {
        return Objects.hash(id, title, completed);
    }
}