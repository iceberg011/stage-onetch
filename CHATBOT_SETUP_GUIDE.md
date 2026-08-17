# OneTech Chatbot - Google Gemini API Setup Guide

## Issue: "I am having trouble reaching the assistant right now"

This error means the chatbot cannot connect to the Google Generative AI API.

## Quick Fix Steps

### Step 1: Verify Your API Key
The current API key in `application.properties` is:
```
AIzaSyB_bPifGUywgsAcwXcnkW4TCC6MkjykRjc
```

### Step 2: Test the API Key
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Log in with your Google account
3. Find the project associated with this API key
4. Go to **APIs & Services** → **Credentials**
5. Find the API key and click it
6. Note the project ID

### Step 3: Enable the Required APIs
1. Go to **APIs & Services** → **Library**
2. Search for **"Generative Language API"** (also called "AI Platform")
3. Click on it and select **ENABLE**
4. Repeat for these APIs if needed:
   - Google AI for Developers API
   - Generative Language API

### Step 4: Check API Key Restrictions
1. In **Credentials**, click your API key
2. Look for **API restrictions**
3. It should either:
   - Have no restrictions (Unrestricted), OR
   - Include "Generative Language API" in the allowed APIs
4. If not, add "Generative Language API" to the allowed list

### Step 5: Check Account Quotas
1. Go to **APIs & Services** → **Quotas**
2. Search for "Generative Language"
3. Verify you haven't exceeded your quota
4. Check the rate limits (default is often quite high)

### Step 6: Validate API Connection
Run this command to test the API key:
```powershell
$apiKey = "AIzaSyB_bPifGUywgsAcwXcnkW4TCC6MkjykRjc"
$url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
$body = @{
    contents = @(
        @{
            parts = @(
                @{ text = "Hello, are you working?" }
            )
        }
    )
}
$response = Invoke-RestMethod -Uri $url -Method Post -ContentType "application/json" -Body ($body | ConvertTo-Json)
$response | ConvertTo-Json
```

## Fallback System

✅ **Good News**: Even if the API is down, the chatbot now includes a fallback response system that provides helpful answers for common questions including:
- Services & Features
- Pricing Plans
- Account Help
- Security Information
- Support Contact Info

## Supported Models (in priority order)

1. **gemini-2.0-flash-exp** - Latest experimental
2. **gemini-2.0-flash** - Latest production
3. **gemini-1.5-pro** - High performance
4. **gemini-1.5-flash** - Fast responses
5. **gemini-pro** - Reliable baseline
6. **gemini-pro-vision** - Multimodal (text + images)
7. **gemini-1.0-pro** - Legacy fallback

The service automatically tries each model in sequence.

## Troubleshooting Logs

When you run the application, check the logs for:
- 🔄 "Attempting model: X" → Current attempt
- ✅ "Successfully used model: X" → Working!
- ❌ "Authentication Failed (401)" → Invalid API key
- ❌ "Access Forbidden (403)" → API not enabled in Google Cloud
- ⏱️ "Rate limit hit" → Too many requests (will auto-retry)
- 🚫 "All models failed" → Falling back to local responses

## Log Files Location
Check application output for detailed error messages showing exactly why the API call failed.

## Contact Support
If you need help:
- Google Support: https://support.google.com/cloud
- OneTech Support: support@onetech.com

---

**Updated**: 2026-08-14
**Version**: 1.0
