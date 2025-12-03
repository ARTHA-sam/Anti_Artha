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

    @Inject
    private AppConfig appConfig;

    // GET /api/products - List all products
    @Step(path = "/products", method = "GET")
    public Object listProducts() throws Exception {
        return productService.getAllProducts();
    }

    // GET /api/products/{id} - Get product by ID
    @Step(path = "/products/{id}", method = "GET")
    public Object getProduct(Request req) throws Exception {
        String id = req.param("id");
        Object product = productService.getProductById(id);

        if (product == null) {
            throw new ProductNotFoundException(id);
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
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Product name is required");
        }

        if (!body.containsKey("price")) {
            throw new InvalidInputException("Product price is required");
        }

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
    public Object updateProduct(Request req) throws Exception {
        String id = req.param("id");
        Map<String, Object> updates = req.bodyAsMap();

        int affected = productService.updateProduct(id, updates);

        if (affected == 0) {
            throw new ProductNotFoundException(id);
        }

        return Map.of("message", "Product updated");
    }

    // DELETE /api/products/{id} - Delete product
    @Step(path = "/products/{id}", method = "DELETE")
    public Object deleteProduct(Request req) throws Exception {
        String id = req.param("id");

        int affected = productService.deleteProduct(id);

        if (affected == 0) {
            throw new ProductNotFoundException(id);
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
                        "Exception Handling",
                        "Configuration Injection",
                        "RESTful API"
                });
    }

    // GET /api/config - Show config values
    @Step(path = "/config", method = "GET")
    public Object getConfig() {
        return Map.of(
                "appName", appConfig.getAppName(),
                "version", appConfig.getAppVersion(),
                "maxProductsPerPage", appConfig.getMaxProductsPerPage(),
                "loggingEnabled", appConfig.isLoggingEnabled(),
                "debugMode", appConfig.isDebugMode(),
                "info", appConfig.getInfo());
    }

    // Exception Handlers

    @ExceptionHandler(ProductNotFoundException.class)
    public Object handleProductNotFound(Exception e, Request req, Response res) {
        res.status(404);
        return Map.of(
                "error", "Not Found",
                "message", e.getMessage(),
                "path", req.path());
    }

    @ExceptionHandler(InvalidInputException.class)
    public Object handleInvalidInput(Exception e, Request req, Response res) {
        res.status(400);
        return Map.of(
                "error", "Bad Request",
                "message", e.getMessage(),
                "path", req.path());
    }
}
