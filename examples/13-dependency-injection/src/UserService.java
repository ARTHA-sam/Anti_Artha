package dev.artha.examples;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class UserService {
    private static int callCount = 0;

    public UserService() {
        System.out.println("UserService created! Instance #" + (++callCount));
    }

    public List<Map<String, String>> getAllUsers() {
        List<Map<String, String>> users = new ArrayList<>();
        users.add(Map.of("id", "1", "name", "Alice"));
        users.add(Map.of("id", "2", "name", "Bob"));
        users.add(Map.of("id", "3", "name", "Charlie"));
        return users;
    }

    public Map<String, String> getUserById(String id) {
        return Map.of("id", id, "name", "User " + id);
    }
}
