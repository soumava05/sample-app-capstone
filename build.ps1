param(
  [string]$WorkDir = (Get-Location).Path
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Set-Location $WorkDir

Write-Host "Building todo-app (Maven)..." -ForegroundColor Cyan
mvn -q clean package

$jarPath = Join-Path $WorkDir "target\todo-app-1.0.0.jar"
if (-not (Test-Path $jarPath)) {
  throw "Expected jar not found: $jarPath"
}

Write-Host "Built artifact: $jarPath" -ForegroundColor Green
