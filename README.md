# AgriX AI — Smart Farmer Assistant

AgriX AI is an Android application built with **Kotlin**, **Jetpack Compose (Material 3)**, **Room SQLite**, and **Retrofit** to assist farmers with real-time localized weather and 5-day forecasts, farm and field management, AI crop recommendations, leaf disease analysis, and an interactive multilingual AI Farming Assistant (English, Hindi, Hinglish, and Punjabi UI localization).

---

## Key Features

1. **Multilingual AI Farming Assistant (`gemini-3.1-flash-lite`)**
   - Automatically detects and replies in the language of the farmer's message (**English**, **Hindi in Devanagari**, or **Hinglish in Roman script**) independently of the app's UI language.
   - Adapts response length dynamically: concise 1–3 sentence answers for simple questions and structured step-by-step guidance when detail is requested.
2. **Plant Disease Analysis**
   - Supports both **Gallery Photo Picker** and **Live Camera Capture** with runtime permission handling.
   - Memory-safe bitmap sampling (`inSampleSize`) and automatic downscaling (`<= 1024px`) before upload.
   - Strict confidence validation (`0..100%`), explicit **Uncertain Diagnosis (`< 60%`)** warnings, and mandatory agricultural safety disclaimers before applying chemical pesticides or fungicides.
3. **Crop Recommendation Engine**
   - Validates farm inputs (land size bounds `0.01..100,000` acres, soil type, water availability, season).
   - Clearly badges results as **AI-Generated Recommendation** or **Local Rule-Based Fallback** (season-, soil-, and water-aware local agronomic rules for Kharif, Rabi, and Zaid).
4. **Live Weather & 5-Day Forecast**
   - Powered by Open-Meteo with explicit Celsius (`°C`) and `km/h` units, real 5-day daily high/low temperatures, weather conditions, and rain probability.
   - Clearly distinguishes between **Live GPS Location** and **Manual / Profile Location**, with Nominatim reverse geocoding rate-limited to 1 req/sec and cached in memory.
5. **Multi-Account Local Authentication & Farm Management**
   - Strict per-user isolation of `UserProfile`, `FarmField`, and `FarmTask` records in Room SQLite.
   - Passwords hashed with `PBKDF2WithHmacSHA256` (65,536 iterations, 16-byte random salt, constant-time verification) and automatic on-login upgrade for legacy SHA-256 hashes.
   - Full support for password change and complete account deletion (wiping user profile, fields, tasks, and credentials).

---

## Project Setup & Configuration

### 1. Environment Variables (`.env`)

Copy `.env.example` to `.env` in the project root (or configure keys in the **AI Studio Secrets Panel**):

```bash
cp .env.example .env
```

Edit `.env` according to your deployment mode:

- **Option A — Production Backend Proxy (Recommended for distributed APKs):**
  Set `AI_BACKEND_BASE_URL` to your backend proxy URL (for example, a Cloud Run / Cloudflare Worker / Node.js proxy that holds your `GEMINI_API_KEY` server-side and forwards requests to `v1beta/models/gemini-3.1-flash-lite:generateContent`):
  ```dotenv
  AI_BACKEND_BASE_URL=https://your-agrix-ai-proxy.example.com
  GEMINI_API_KEY=NONE
  ```
  > **Security Warning:** Any secret compiled into Android `BuildConfig` or APK resources can be extracted by decompiling the APK. Using `AI_BACKEND_BASE_URL` keeps your Gemini API key 100% off the client device.

- **Option B — Local Development / Direct Client Key:**
  For local testing without a proxy server, keep `AI_BACKEND_BASE_URL=NONE` and provide a restricted Gemini API key in `.env`:
  ```dotenv
  AI_BACKEND_BASE_URL=NONE
  GEMINI_API_KEY=your_actual_gemini_api_key_here
  ```

---

## Authentication Architecture (Local On-Device Accounts)

AgriX AI uses **Local On-Device Authentication** backed by Android Jetpack Room (`users` table) and `SharedPreferences` session state:
- Accounts created in the app are stored locally on the device and do **not** sync across multiple phones or cloud servers.
- Passwords are salted and hashed using `PBKDF2WithHmacSHA256` (`SecurityUtils.kt`).
- Each logged-in account has isolated farm profile, field, and task records (`userId` foreign/logical key).

---

## Building, Testing, and Linting

### Standard Environment (Gradle CLI)
In the cloud container environment, run Gradle directly:

```bash
# Assemble debug APK
gradle :app:assembleDebug

# Run unit and Robolectric tests
gradle :app:testDebugUnitTest

# Run Android Lint
gradle :app:lintDebug
```

If using a local workstation with the Gradle wrapper:
```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```

---

## Known Limitations & Platform Notes

1. **Local-Only Accounts**: User accounts and farm data are stored in local Room SQLite (`agrix_database`). Uninstalling the app or clearing app storage removes local accounts unless backed up.
2. **Advisory Nature of AI Diagnosis**: AI crop and leaf disease analyses are for informational and educational decision support only. Always consult a certified local agronomist or Krishi Vigyan Kendra (KVK) before applying chemical pesticides or fertilizers.
3. **Signing Configuration in AI Studio Container**: The AI Studio build environment manages `debug.keystore` automatically at build time. For external release builds outside AI Studio, supply signing credentials via secure CI environment variables rather than committing keystore files to Git.
