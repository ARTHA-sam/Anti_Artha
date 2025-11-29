# Fix VS Code Configuration for Existing Project
# Run this script from your project directory (Check folder)

# Create .vscode folder if it doesn't exist
New-Item -Path ".vscode" -ItemType Directory -Force | Out-Null

# Create settings.json
$settings = @"
{
    "java.project.sourcePaths": [
        "src"
    ],
    "java.project.outputPath": "build",
    "java.project.referencedLibraries": [
        ".artha/lib/**/*.jar",
        "lib/artha-runtime.jar"
    ]
}
"@

Set-Content -Path ".vscode\settings.json" -Value $settings

Write-Host "✅ VS Code configuration updated!" -ForegroundColor Green
Write-Host "Now reload VS Code window: Ctrl+Shift+P → 'Reload Window'" -ForegroundColor Yellow
