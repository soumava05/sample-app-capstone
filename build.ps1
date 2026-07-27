param(
  [string]$JarName = "todo-app-1.0.0.jar"
)

$ErrorActionPreference = "Stop"

Write-Host "Building with Maven..."
mvn clean package
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$target = Join-Path $PSScriptRoot "target"
$jar = Join-Path $target $JarName

if (!(Test-Path $jar)) {
  Write-Error "Expected jar not found: $jar"
  Write-Host "Available artifacts in target/:"
  Get-ChildItem -Path $target -File | ForEach-Object { Write-Host " - $($_.Name)" }
  exit 1
}

Write-Host "Build successful. Jar: $jar"
