# Comprehensive Demo

This example demonstrates **all ARTHA framework features** in one application.

## Features Demonstrated

✅ **REST API** - Full CRUD operations for products  
✅ **Middleware** - Logging middleware on all requests  
✅ **Dependency Injection** - Services injected into controllers  
✅ **Query Builder** - SQL-safe database operations  
✅ **Exception Handling** - Custom handlers for errors  
✅ **Configuration Injection** - Config values from artha.json  
✅ **Scheduled Tasks** - Background jobs running automatically  

## Running the Demo

```bash
# From artha root directory

# Compile
javac -cp runtime/target/artha-runtime-0.1.0.jar -d examples/comprehensive-demo/build examples/comprehensive-demo/src/*.java

# Run
cd examples/comprehensive-demo
java -cp "../../runtime/target/artha-runtime-0.1.0.jar;build" dev.artha.core.Runtime
```

Server starts at `http://localhost:8080`

## API Endpoints

### Products API

```bash
# List all products
GET http://localhost:8080/api/products

# Get product by ID
GET http://localhost:8080/api/products/1

# Search products
GET http://localhost:8080/api/products/search?q=laptop

# Create product
POST http://localhost:8080/api/products
Content-Type: application/json
{
  "name": "New Product",
  "price": 99.99,
  "stock": 100
}

# Update product
PUT http://localhost:8080/api/products/1
Content-Type: application/json
{
  "price": 89.99
}

# Delete product
DELETE http://localhost:8080/api/products/1
```

### System Endpoints

```bash
# Health check
GET http://localhost:8080/api/health

# View configuration
GET http://localhost:8080/api/config
```

## File Structure

```
comprehensive-demo/
├── src/
│   ├── DemoController.java        # Main REST controller
│   ├── ProductService.java        # Service using Query Builder
│   ├── AppConfig.java             # Config injection demo
│   ├── BackgroundTasks.java       # Scheduled tasks demo
│   ├── LoggingMiddleware.java    # Logging middleware
│   ├── ProductNotFoundException.java
│   └── InvalidInputException.java
├── artha.json                     # Configuration
└── README.md
```

## What You'll See

### Console Output

```
🔍 Scanning for routes...

  🛡️  ProductNotFoundException → DemoController.handleProductNotFound()
  🛡️  InvalidInputException → DemoController.handleInvalidInput()

⏰ Registering scheduled tasks...

  ⏰ Scheduled: BackgroundTasks.heartbeat() every 10000ms
  ⏰ Scheduled: BackgroundTasks.cleanupTask() every 30000ms
  ⏰ Scheduled: BackgroundTasks.statusCheck() every 5000ms

✓ Server started at http://localhost:8080

📝 [LOG] GET /api/health
💓 [Heartbeat #1] ARTHA Demo is alive!
✓ [Status] All systems operational
```

### Error Handling

When you request a non-existent product:

```bash
GET /api/products/999
```

Response:
```json
{
  "error": "Not Found",
  "message": "Product with ID 999 not found",
  "path": "/api/products/999"
}
```

## Learning Points

This demo teaches:
1. **Separation of Concerns** - Controllers, Services, Models
2. **Dependency Injection** - Loose coupling between components
3. **Database Best Practices** - Query Builder instead of raw SQL
4. **Error Handling** - Centralized exception management
5. **Configuration Management** - Externalized configuration
6. **Background Processing** - Scheduled tasks for maintenance
7. **Middleware Pattern** - Cross-cutting concerns

**Perfect for learning modern backend development!** 🎓
