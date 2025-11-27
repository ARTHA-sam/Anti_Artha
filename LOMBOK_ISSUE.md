# Lombok Support (Known Issue)

## Current Status

Lombok has compatibility issues with JDK 21+ that cause compilation errors even with the proper JVM flags configured in the ARTHA CLI.

## Error You Might See

```
java.lang.NoSuchFieldException: com.sun.tools.javac.code.TypeTag :: UNKNOWN
java.lang.ExceptionInInitializerError
at lombok.javac.apt.LombokProcessor...
```

## Workaround

**Option 1: Use Manual Getters/Setters (Recommended)**
```java
public class User {
    private String name;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

**Option 2: Use JDK 17 or 11**
If you need Lombok, downgrade to JDK 17 LTS or JDK 11 LTS.

**Option 3: Wait for Lombok Update**
Monitor Lombok releases for full JDK 21+ support.

## Technical Details

The ARTHA CLI (`dev.js`) already includes the necessary JVM flags for Lombok:
- `--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED`
- `--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED`
- And 11 more...

However, Lombok 1.18.36 (latest as of Nov 2024) still has internal compatibility issues with JDK 21's module system.
