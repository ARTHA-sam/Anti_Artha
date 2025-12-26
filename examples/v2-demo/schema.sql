-- ARTHAv2.0 Demo - User table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    age INTEGER NOT NULL
);

-- Sample data
INSERT OR IGNORE INTO users (id, name, email, age) VALUES
(1, 'John Doe', 'john@example.com', 25),
(2, 'Jane Smith', 'jane@example.com', 30),
(3, 'Bob Johnson', 'bob@example.com', 22);
