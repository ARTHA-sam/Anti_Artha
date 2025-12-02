package demo;

import dev.artha.annotations.*;
import dev.artha.http.Request;
import dev.artha.http.Response;
import java.util.Map;

/**
 * Comprehensive demo showing:
 * - Middleware (@Before/@After)
 * - Dependency Injection (@Inject)
 * - Query Builder (ProductService)
 * - Method-level routing
 */
@Step(path = "/api")
@Before({ LoggingMiddleware.class })
public class DemoController {

    @Inject
    private ProductService productService;

    // GET /api/products - List all products
    @Step(path = "/products", method = "GET")
    public Object listProducts() throws Exception {
        return productService.getAllProducts();
    }

    // GET /api/products/{id} - Get product by ID
    @Step(path = "/products/{id}", method = "GET")
    public Object getProduct(Request req, Response res) throws Exception {
        String id = req.param("id");
        Object product = productService.getProductById(id);

        if (product == null) {
            res.status(404);
            return Map.of("error", "Product not found");
        }

        return product;
    }

    // GET /api/products/search?q=laptop - Search products
    @Step(path = "/products/search", method = "GET")
    public Object searchProducts(Request req) throws Exception {
        String query = req.query("q");
        if (query == null || query.isEmpty()) {
            return productService.getAllProducts();
        }
        return productService.searchProducts(query);
    }

    // POST /api/products - Create product
    @Step(path = "/products", method = "POST")
    public Object createProduct(Request req, Response res) throws Exception {
        Map<String, Object> body = req.bodyAsMap();

        String name = (String) body.get("name");
        double price = ((Number) body.get("price")).doubleValue();
        int stock = ((Number) body.getOrDefault("stock", 0)).intValue();

        int id = productService.createProduct(name, price, stock);

        res.status(201);
        return Map.of(
                "message", "Product created",
                "id", id);
    }

    // PUT /api/products/{id} - Update product
    @Step(path = "/products/{id}", method = "PUT")
    public Object updateProduct(Request req, Response res) throws Exception {
        String id = req.param("id");
        Map<String, Object> updates = req.bodyAsMap();

        int affected = productService.updateProduct(id, updates);

        if (affected == 0) {
            res.status(404);
            return Map.of("error", "Product not found");
        }

        return Map.of("message", "Product updated");
    }

    // DELETE /api/products/{id} - Delete product
    @Step(path = "/products/{id}", method = "DELETE")
    public Object deleteProduct(Request req, Response res) throws Exception {
        String id = req.param("id");

        int affected = productService.deleteProduct(id);

        if (affected == 0) {
            res.status(404);
            return Map.of("error", "Product not found");
        }

        return Map.of("message", "Product deleted");
    }

    // GET /api/health - Health check
    @Step(path = "/health", method = "GET")
    public Object health() {
        return Map.of(
                "status", "UP",
                "features", new String[] {
                        "Middleware",
                        "Dependency Injection",
                        "Query Builder",
                        "RESTful API"
                });
    }
}
