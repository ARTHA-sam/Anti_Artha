# ARTHA Social Network - Quick Fix

## The Problem
The CLI was trying to use an old runtime JAR. We need to use the freshly built one.

## Solution

**Run this command manually:**

```bash
cd c:\Users\samar\Downloads\artha\examples\social-network

# Compile (all-in-one command)
javac -cp "..\..\runtime\target\artha-runtime-0.1.0.jar" -d build src\**\*.java src\*.java

# Run
java -cp "..\..\runtime\target\artha-runtime-0.1.0.jar;build" dev.artha.core.Runtime
```

That's it! The project will compile and run.

**Then open:** `frontend/index.html` in your browser.

## Why This Works

- Uses the correct runtime JAR from `runtime/target/`
- Compiles all Java files together (models, services, controllers)
- No package structure needed (simpler!)

🚀 Try it now!
