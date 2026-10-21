# AgriX External Services Manifest (Free-Tier Verification)

AgriX is engineered to operate 100% on free-tier, open-source, or non-commercial public APIs. It complies strictly with the "Free APIs Only" mandate, with zero risk of automated billing or mandatory subscription requirements.

---

### 1. Open-Meteo Forecast API
- **Service Name**: Open-Meteo Weather API
- **AgriX Usage**: Fetches real-time current weather metrics (temperature, humidity, wind speed, rain probability) and a 5-day forecast for the user's active farm location.
- **Free/Free-Tier Status**: 100% Free for non-commercial use.
- **Required API Key**: None (public endpoint).
- **Important Rate Limits**: Max 10,000 requests per day per IP address.
- **Billing Required**: No billing or credit card required.

---

### 2. Google Gemini API (gemini-3.5-flash)
- **Service Name**: Google AI Studio Gemini API
- **AgriX Usage**: 
  1. **AI Farming Assistant**: Generates dynamic context-aware advice on crops, pests, fertilizers, and irrigation in English, Hindi, and Punjabi.
  2. **Plant Disease Cloud AI**: Performs real-time computer-vision diagnosis on crop leaf photographs uploaded by the farmer.
- **Free/Free-Tier Status**: Free Tier available.
- **Required API Key**: Yes (Secured via `BuildConfig.GEMINI_API_KEY`, injected at compile time from user secrets).
- **Important Rate Limits**: 
  - 15 RPM (Requests Per Minute)
  - 1,000,000 TPM (Tokens Per Minute)
  - 1,500 RPD (Requests Per Day)
- **Billing Required**: No credit card or billing profile required.

---

### 3. OpenStreetMap Nominatim Geocoding API
- **Service Name**: Nominatim OpenStreetMap Search API
- **AgriX Usage**: Resolves coordinate pairs (latitude and longitude) into real human-readable village, city, and state names dynamically.
- **Free/Free-Tier Status**: Free public usage.
- **Required API Key**: None.
- **Important Rate Limits**: Max 1 request per second.
- **Billing Required**: No billing required.

---

### 4. Room Local SQLite Database
- **Service Name**: Android Jetpack Room Database
- **AgriX Usage**: Fully local persistence for farmer profiles, registered fields, task statuses, offline guides, and active language preferences.
- **Free/Free-Tier Status**: 100% Free / Open-source (runs natively on-device).
- **Required API Key**: None.
- **Important Rate Limits**: None (bounded only by local device storage limits).
- **Billing Required**: No billing required.
