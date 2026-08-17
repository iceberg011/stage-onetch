#!/usr/bin/env pwsh
# OneTech Chatbot - Gemini API Diagnostic Test
# This script tests if your Gemini API key is working

param(
    [string]$ApiKey = "AIzaSyB_bPifGUywgsAcwXcnkW4TCC6MkjykRjc",
    [string]$Model = "gemini-1.5-flash"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "OneTech Chatbot - API Diagnostic Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check API Key Format
Write-Host "[1] Checking API Key Format..." -ForegroundColor Yellow
if ($ApiKey -match "^AIza[A-Za-z0-9_-]{35}$") {
    Write-Host "✅ API Key format is valid" -ForegroundColor Green
} else {
    Write-Host "❌ API Key format appears invalid" -ForegroundColor Red
    Write-Host "   Expected: AIza + 35 alphanumeric characters" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Step 2: Check Internet Connectivity
Write-Host "[2] Checking Internet Connectivity..." -ForegroundColor Yellow
try {
    $testConnection = Test-NetConnection -ComputerName generativelanguage.googleapis.com -Port 443 -ErrorAction Stop
    if ($testConnection.TcpTestSucceeded) {
        Write-Host "✅ Can reach generativelanguage.googleapis.com" -ForegroundColor Green
    } else {
        Write-Host "❌ Cannot reach API server" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Network connectivity test failed: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Step 3: Test API Request
Write-Host "[3] Testing API Request with $Model..." -ForegroundColor Yellow
$url = "https://generativelanguage.googleapis.com/v1beta/models/${Model}:generateContent?key=$ApiKey"

$requestBody = @{
    contents = @(
        @{
            parts = @(
                @{ text = "Hello! Are you working?" }
            )
        }
    )
    generationConfig = @{
        temperature = 0.7
        maxOutputTokens = 100
    }
} | ConvertTo-Json

Write-Host "   URL: $url" -ForegroundColor Gray
Write-Host "   Model: $Model" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $url `
        -Method Post `
        -ContentType "application/json" `
        -Body $requestBody `
        -UseBasicParsing `
        -TimeoutSec 30 `
        -ErrorAction Stop

    Write-Host "✅ API Response: $($response.StatusCode)" -ForegroundColor Green
    
    # Parse response
    $content = $response.Content | ConvertFrom-Json
    
    if ($content.error) {
        Write-Host "❌ API Error: $($content.error.code)" -ForegroundColor Red
        Write-Host "   Message: $($content.error.message)" -ForegroundColor Red
        
        if ($content.error.code -eq 401) {
            Write-Host "   💡 Fix: Check your API key in application.properties" -ForegroundColor Yellow
        } elseif ($content.error.code -eq 403) {
            Write-Host "   💡 Fix: Enable 'Generative Language API' in Google Cloud Console" -ForegroundColor Yellow
        } elseif ($content.error.code -eq 429) {
            Write-Host "   💡 Fix: Rate limit exceeded. Try again in a few moments." -ForegroundColor Yellow
        }
    } else {
        Write-Host "✅ Successfully received AI response!" -ForegroundColor Green
        
        if ($content.candidates -and $content.candidates.Count -gt 0) {
            $answer = $content.candidates[0].content.parts[0].text
            Write-Host "   Response: $answer" -ForegroundColor Green
        }
    }
    
} catch [System.Net.WebException] {
    $statusCode = [int]$_.Exception.Response.StatusCode
    $errorBody = $_.Exception.Response | ForEach-Object { $_.GetResponseStream() | ForEach-Object { [System.IO.StreamReader]::new($_).ReadToEnd() } }
    
    Write-Host "❌ HTTP Error $statusCode" -ForegroundColor Red
    Write-Host "   Error: $errorBody" -ForegroundColor Red
    
    if ($statusCode -eq 401) {
        Write-Host "   💡 Authentication Failed - Invalid or expired API key" -ForegroundColor Yellow
    } elseif ($statusCode -eq 403) {
        Write-Host "   💡 Access Denied - API not enabled or quota exceeded" -ForegroundColor Yellow
    } elseif ($statusCode -eq 429) {
        Write-Host "   💡 Rate Limited - Too many requests" -ForegroundColor Yellow
    }
    exit 1
    
} catch {
    Write-Host "❌ Connection Error: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Message -like "*timeout*" -or $_.Exception.Message -like "*time out*") {
        Write-Host "   💡 Timeout - Check your internet connection or firewall" -ForegroundColor Yellow
    }
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "✅ All tests passed! API is working." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
