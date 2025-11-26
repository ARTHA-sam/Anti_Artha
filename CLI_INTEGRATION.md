# ARTHA CLI Integration Guide

## Dependency Management Workflow

### How Dependencies are Resolved

The ARTHA CLI will automatically manage dependencies declared in `artha.json`:

#### Step 1: Read Configuration
```bash
artha run
```

The CLI reads `artha.json` from the project root:
```json
{
  "port": 8080,
  "env": "dev",
  "dependencies": [
    "lombok",
    "postgresql",
    "mysql-connector"
  ]
}
```

#### Step 2: Resolve Dependencies
For each dependency name, the CLI:
1. Maps it to a Maven coordinate (e.g., `lombok` → `org.projectlombok:lombok:1.18.32`)
2. Checks if `.artha/libs/lombok-1.18.32.jar` exists
3. If missing, downloads from Maven Central

**Download Command:**
```bash
curl -L -o .artha/libs/lombok-1.18.32.jar \
  https://repo1.maven.org/maven2/org/projectlombok/lombok/1.18.32/lombok-1.18.32.jar
```

#### Step 3: Compile with Dependencies
```bash
javac -cp ".artha/libs/*:runtime/target/classes" \
      -d build \
      src/**/*.java
```

**Breakdown:**
- `-cp .artha/libs/*` - Include all downloaded JARs
- `-cp runtime/target/classes` - Include ARTHA runtime
- `-d build` - Output directory
- `src/**/*.java` - User code

#### Step 4: Run Application
```bash
java -cp "build:.artha/libs/*:runtime/target/classes" \
     -Dartha.port=8080 \
     dev.artha.core.Runtime
```

**Breakdown:**
- `-cp build` - Compiled user code
- `-cp .artha/libs/*` - Downloaded dependencies
- `-cp runtime/target/classes` - ARTHA runtime
- `-Dartha.port=8080` - Port configuration
- `dev.artha.core.Runtime` - Main entry point

### CLI Commands

#### `artha run`
Compiles and runs the application:
1. Load `artha.json`
2. Download missing dependencies
3. Compile all `.java` files
4. Start Runtime server

#### `artha deps install`
Downloads dependencies without running:
```bash
artha deps install
```

#### `artha deps clean`
Removes `.artha/libs` directory:
```bash
artha deps clean
```

### Dependency Cache

Dependencies are cached in `.artha/libs/`:
```
.artha/
└── libs/
    ├── lombok-1.18.32.jar
    ├── postgresql-42.7.3.jar
    └── mysql-connector-j-8.3.0.jar
```

**Benefits:**
- Downloaded once, reused across runs
- Faster subsequent builds
- Offline support after initial download

### Adding Custom Dependencies

Users can specify Maven coordinates directly in `artha.json`:

```json
{
  "dependencies": [
    "com.google.guava:guava:33.0.0-jre",
    "org.apache.commons:commons-lang3:3.14.0"
  ]
}
```

The CLI recognizes both:
1. **Short names**: `"lombok"` (mapped internally)
2. **Full coordinates**: `"group:artifact:version"`

### Error Handling

**Missing Dependency Mapping:**
```
⚠️  Unknown dependency: 'redis' — skipping
    Add it manually: "org.redisson:redisson:3.25.2"
```

**Download Failure:**
```
❌ Failed to download postgresql-42.7.3.jar
   Check internet connection
   URL: https://repo1.maven.org/maven2/...
```

**Compilation Error:**
```
❌ Compilation failed
   Missing dependency for import: org.postgresql.Driver
   Add to artha.json: "postgresql"
```

### Integration with Runtime

The Runtime class now:
1. Loads `ArthaConfig` before starting
2. Calls `DependencyLoader.loadDependencies()`
3. Downloads and adds JARs to classpath
4. Proceeds with normal startup

**Modified Runtime startup:**
```java
public static void main(String[] args) {
    printBanner();
    
    // Load config and dependencies
    ArthaConfig config = ArthaConfig.load();
    DependencyLoader.loadDependencies(config.getDependencies());
    
    // Use config port or system property
    int port = Integer.parseInt(
        System.getProperty("artha.port", String.valueOf(config.getPort()))
    );
    
    // ... rest of startup
}
```

### Development Workflow

1. **Create project structure:**
   ```bash
   mkdir my-api && cd my-api
   mkdir src
   ```

2. **Create artha.json:**
   ```json
   {
     "port": 3000,
     "dependencies": ["postgresql", "lombok"]
   }
   ```

3. **Write code:**
   ```java
   // src/Users.java
   @Step(path = "/users")
   public class Users {
       public List<User> handle(Request req, Response res) {
           // PostgreSQL code here
       }
   }
   ```

4. **Run:**
   ```bash
   artha run
   ```

   Output:
   ```
   📦 Loading dependencies...
     ⬇ Downloading postgresql...
       ✓ Downloaded postgresql-42.7.3.jar
     ✓ Found lombok-1.18.32.jar
   
   🔍 Scanning for routes...
     GET  /users → Users
   
   ✓ Server started at http://localhost:3000
   ```

### Production Deployment

For production, pre-download dependencies:

```bash
# On CI/CD server
artha deps install

# Build fat JAR
artha build --jar

# Deploy
java -jar build/my-api.jar
```

This generates a standalone JAR with all dependencies embedded.
