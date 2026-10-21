# AgriX External Services Manifest (Architecture & Security Verification)

AgriX AI is engineered to operate on free-tier, open-source, or non-commercial public APIs while supporting a secure backend proxy architecture for AI inference.

---

### 1. Open-Meteo Forecast API
- **Service Name**: Open-Meteo Weather API (`https://api.open-meteo.com/v1/forecast`)
- **AgriX Usage**: Fetches real-time current weather metrics (`temperature_2m`, `relative_humidity_2m`, `wind_speed_10m`, `weather_code`), hourly precipitation probability, and a real 5-day daily forecast (`weather_code`, `temperature_2m_max`, `temperature_2m_min`, `precipitation_probability_max`) with explicit units (`temperature_unit=celsius`, `wind_speed_unit=kmh`, `timezone=auto`).
- **Free/Free-Tier Status**: 100% Free for non-commercial use.
- **Required API Key**: None (public endpoint).
- **Important Rate Limits & Timeouts**: Max 10,000 requests per day per IP address; configured with 15s network timeouts and coordinate bounds validation (`-90..90` lat, `-180..180` lon).
- **Billing Required**: No billing or credit card required.

---

### 2. Google Gemini API (`gemini-3.1-flash-lite`)
- **Service Name**: Google Gemini Generative Language API
- **Model Identifier**: `gemini-3.1-flash-lite` (standard `v1beta` endpoint)
- **AgriX Usage**:
  1. **AI Farming Assistant**: Generates concise, context-aware advice on crops, pests, fertilizers, and irrigation with automatic per-message language detection (English, Hindi in Devanagari, and Hinglish in Roman script).
  2. **Crop Recommendation Engine**: Produces tailored crop recommendations with an explicit `AI-Generated Recommendation` label, falling back to a transparent `Local Rule-Based Fallback` when offline or unconfigured.
  3. **Plant Disease Cloud AI**: Performs computer-vision leaf analysis with downscaled image payloads (max 1024px), strict confidence validation (`0..100`), uncertainty detection (`< 60%` confidence), and mandatory safety disclaimers for chemical treatments.
- **Security & Key Management**:
  - **Production Mode (`AI_BACKEND_BASE_URL`)**: Routes requests through a configurable backend proxy (`POST {AI_BACKEND_BASE_URL}/v1beta/models/gemini-3.1-flash-lite:generateContent`) so the Gemini API key stays exclusively in server-side environment secrets and is never embedded in the Android APK.
  - **Local Development Mode (`GEMINI_API_KEY`)**: When `AI_BACKEND_BASE_URL` is blank, uses `BuildConfig.GEMINI_API_KEY` injected at build time from `.env`. **Important Security Note**: Any value compiled into Android `BuildConfig` or client resources can be extracted from a distributed APK; therefore, direct `GEMINI_API_KEY` injection is suitable only for local development/testing, not public APK distribution.
- **Rate Limits & Safeguards**:
  - Bounded user prompt lengths (`MAX_USER_PROMPT_CHARS = 2000`) and bounded conversation history.
  - Configured OkHttp timeouts (20s connect, 30s read, 20s write).
  - Clean user-facing error handling for HTTP `401`/`403` (invalid/unauthorized key), `429` (rate limit exceeded), `5xx` (server error), and network timeouts without leaking stack traces or keys.

---

### 3. OpenStreetMap Nominatim Geocoding API
- **Service Name**: Nominatim OpenStreetMap Reverse & Search API (`https://nominatim.openstreetmap.org/`)
- **AgriX Usage**: Resolves GPS coordinates (`lat`, `lon`) into human-readable village, district, and state names, and allows manual location search.
- **Compliance & Rate Limiting**:
  - Enforces a minimum 1,000 ms interval (max 1 request/second) via a thread-safe coroutine `Mutex` in `GeocodingApiService`.
  - Caches reverse-geocoding (rounded to ~110m `0.001` degree precision) and search queries in-memory to eliminate redundant network calls.
  - Sends a compliant, descriptive `User-Agent` header (`AgriX-AI-Farmer-Assistant/1.0 (Android; contact: support@agrix-ai.org)`).
- **Free/Free-Tier Status**: Free public usage under OSM Nominatim Usage Policy.
- **Required API Key**: None.

---

### 4. Room Local SQLite Database & Local Authentication
- **Service Name**: Android Jetpack Room Database (`agrix_database`, schema v3)
- **AgriX Usage**: Local on-device persistence for user accounts, farmer profiles, registered fields, farm tasks, and offline farming guides.
- **Data Integrity & Security**:
  - **Password Hashing**: Uses versioned `PBKDF2WithHmacSHA256` (`v2$65536$<base64Salt+Hash>`) with a 16-byte `SecureRandom` salt, 65,536 iterations, and constant-time comparison (`MessageDigest.isEqual`). Legacy v1 hashes are transparently upgraded to v2 PBKDF2 upon successful login.
  - **Non-Destructive Migrations**: Explicit `MIGRATION_1_2` and `MIGRATION_2_3` preserve all existing user accounts, farms, and tasks across schema upgrades (`fallbackToDestructiveMigration` is removed).
  - **Strict Account Isolation**: All profiles, fields, and tasks are scoped strictly to the authenticated `userId` (`currentUserId`).
