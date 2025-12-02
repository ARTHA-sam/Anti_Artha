package demo;

import dev.artha.db.Database;
import dev.artha.db.QueryBuilder;
import java.util.List;
import java.util.Map;

public class ProductService {

    public ProductService() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            // Create products table if not exists
            Database.getInstance().getConnection().createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS products (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "name TEXT NOT NULL, " +
                            "price REAL NOT NULL, " +
                            "stock INTEGER DEFAULT 0)");

            // Add sample data if table is empty
            List<Map<String, Object>> existing = Database.getInstance()
                    .table("products")
                    .limit(1)
                    .get();

            if (existing.isEmpty()) {
                Database.getInstance().table("products")
                        .insert(Map.of("name", "Laptop", "price", 999.99, "stock", 50));
                Database.getInstance().table("products")
                        .insert(Map.of("name", "Mouse", "price", 29.99, "stock", 200));
                Database.getInstance().table("products")
                        .insert(Map.of("name", "Keyboard", "price", 79.99, "stock", 150));

                System.out.println("✅ Sample products added");
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getAllProducts() throws Exception {
        return Database.getInstance()
                .table("products")
                .orderBy("name")
                .get();
    }

    public Map<String, Object> getProductById(String id) throws Exception {
        List<Map<String, Object>> results = Database.getInstance()
                .table("products")
                .where("id", id)
                .get();
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Map<String, Object>> searchProducts(String keyword) throws Exception {
        return Database.getInstance()
                .table("products")
                .where("name", "LIKE", "%" + keyword + "%")
                .orderBy("price", "ASC")
                .get();
    }

    public int createProduct(String name, double price, int stock) throws Exception {
        return Database.getInstance()
                .table("products")
                .insert(Map.of("name", name, "price", price, "stock", stock));
    }

    public int updateProduct(String id, Map<String, Object> updates) throws Exception {
        return Database.getInstance()
                .table("products")
                .where("id", id)
                .update(updates);
    }

    public int deleteProduct(String id) throws Exception {
        return Database.getInstance()
                .table("products")
                .where("id", id)
                .delete();
    }
}
