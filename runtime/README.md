# ARTHA Runtime

The core framework runtime containing all annotations, core components, and HTTP handling.

## Building

```bash
mvn clean package
```

Output: `target/artha-runtime-0.1.0.jar`

## Module Structure

```
runtime/src/main/java/dev/artha/
├── annotations/           # Framework annotations
│   ├── Step.java         # Routing
│   ├── Inject.java       # Dependency injection
│   ├── Before.java       # Middleware (before)
│   ├── After.java        # Middleware (after)
│   ├── ExceptionHandler.java  # Exception handling
│   ├── ConfigValue.java  # Configuration injection
│   ├── Scheduled.java    # Scheduled tasks
│   └── Valid.java        # Validation
│
├── core/                  # Core components
│   ├── Runtime.java      # Main entry point
│   ├── DIContainer.java  # Dependency injection container
│   ├── ConfigManager.java # Configuration management
│   └── TaskScheduler.java # Scheduled task executor
│
├── db/                    # Database components
│   ├── Database.java     # Connection pool manager
│   └── QueryBuilder.java # Fluent query builder
│
└── http/                  # HTTP layer
    ├── Request.java      # Request interface
    ├── RequestImpl.java  # Request implementation
    ├── Response.java     # Response interface
    ├── ResponseImpl.java # Response implementation
    └── Middleware.java   # Middleware interface
```

## Dependencies

- **Javalin** - Embedded web server
- **Jackson** - JSON serialization
- **HikariCP** - Connection pooling
- **Hibernate Validator** - Bean validation
- **Reflections** - Annotation scanning
- **SLF4J** - Logging

## Adding to Your Project

```bash
java -cp "path/to/artha-runtime-0.1.0.jar;your-classes" dev.artha.core.Runtime
```

## Development

### Running Tests

```bash
mvn test
```

### Creating a Release

```bash
mvn clean package
```

The shaded JAR will include all dependencies.
