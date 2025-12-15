# Stop any running Java processes
taskkill /F /IM java.exe 2>$null

# Clean build directory
Remove-Item -Recurse -Force build -ErrorAction SilentlyContinue

# Compile
Write-Host "Compiling..."
javac -cp "lib-local\artha-runtime-0.1.0.jar;.artha\lib\*" -d build src\*.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!"
    exit 1
}

# Run
Write-Host "Starting Server..."
java -cp "build;lib-local\artha-runtime-0.1.0.jar;.artha\lib\*" dev.artha.core.Runtime
