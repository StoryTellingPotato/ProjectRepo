# update-java.ps1
# Run this script as Administrator

param(
    [string]$JavaVersion = "21"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "    Java Update Script for Windows" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if running as admin
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "✗ Please run this script as Administrator!" -ForegroundColor Red
    Write-Host "  Right-click PowerShell → Run as Administrator" -ForegroundColor Yellow
    exit 1
}

# Get current version
Write-Host "Current Java version:" -ForegroundColor Yellow
try {
    $currentVersion = java -version 2>&1
    Write-Host $currentVersion -ForegroundColor Gray
} catch {
    Write-Host "Java not found or not in PATH" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Downloading Java $JavaVersion..." -ForegroundColor Yellow

# Create temp directory
$tempDir = "$env:TEMP\java-update"
New-Item -ItemType Directory -Force -Path $tempDir | Out-Null
cd $tempDir

# Download OpenJDK
$downloadUrl = "https://github.com/adoptium/temurin$JavaVersion-binaries/releases/latest/download/OpenJDK${JavaVersion}U-jdk_x64_windows_hotspot_latest.zip"
$zipFile = "$tempDir\openjdk-$JavaVersion.zip"

try {
    Invoke-WebRequest -Uri $downloadUrl -OutFile $zipFile -UseBasicParsing -ErrorAction Stop
    Write-Host "✓ Download complete" -ForegroundColor Green
} catch {
    Write-Host "✗ Download failed. Trying alternative URL..." -ForegroundColor Yellow
    
    # Alternative URL for Java 21
    $altUrl = "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.5%2B11/OpenJDK21U-jdk_x64_windows_hotspot_21.0.5_11.zip"
    Invoke-WebRequest -Uri $altUrl -OutFile $zipFile -UseBasicParsing
    Write-Host "✓ Download complete from alternative URL" -ForegroundColor Green
}

# Extract Java
Write-Host "Extracting Java..." -ForegroundColor Yellow
$extractPath = "C:\Program Files\Eclipse Adoptium"
Expand-Archive -Path $zipFile -DestinationPath $extractPath -Force

# Find the extracted folder
$javaHome = Get-ChildItem -Path $extractPath -Directory | Where-Object { $_.Name -like "jdk-*" } | Select-Object -First 1
$javaHomePath = $javaHome.FullName

# Set environment variables
Write-Host "Setting environment variables..." -ForegroundColor Yellow
[Environment]::SetEnvironmentVariable("JAVA_HOME", $javaHomePath, "Machine")

$currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
if ($currentPath -notlike "*$javaHomePath\bin*") {
    [Environment]::SetEnvironmentVariable("Path", "$currentPath;$javaHomePath\bin", "Machine")
}

# Update current session
$env:JAVA_HOME = $javaHomePath
$env:Path = "$env:Path;$javaHomePath\bin"

# Verify installation
Write-Host ""
Write-Host "Verifying installation..." -ForegroundColor Yellow
refreshenv 2>$null

Write-Host ""
Write-Host "New Java version:" -ForegroundColor Green
java -version

Write-Host ""
Write-Host "Java Home: $env:JAVA_HOME" -ForegroundColor Gray
Write-Host ""
Write-Host "✓ Java update complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Please restart your terminal or VS Code for changes to take effect." -ForegroundColor Yellow

# Cleanup
Remove-Item -Path $tempDir -Recurse -Force -ErrorAction SilentlyContinue