# AgriX AI — Smart Farmer Assistant

AgriX AI is an Android smart-farming assistant built with Kotlin, Jetpack Compose, Room SQLite, and Retrofit. It includes local farm management, localized weather, crop recommendations, plant disease analysis, and a multilingual AI farming assistant.

## Features

- Multilingual farming assistant (English, Hindi, and Hinglish).
- Plant disease analysis with uncertainty messaging and agricultural safety disclaimers.
- Crop recommendations with local rule-based fallback when AI is unavailable.
- Current weather and a five-day forecast using Open-Meteo.
- Farm profile, fields, and tasks stored locally in Room.
- On-device account management with salted PBKDF2-HMAC-SHA256 password hashes.

## Public-client security model

**The Android client intentionally does not contain a Gemini API key.** AI features require an HTTPS backend proxy configured with `AI_BACKEND_BASE_URL`. The proxy must keep `GEMINI_API_KEY` in server-side secrets, authenticate or otherwise control callers, enforce request and cost limits, validate inputs, and avoid logging sensitive prompts or image contents.

Do not configure a real `GEMINI_API_KEY` in Android `BuildConfig`, resources, source files, or APK assets. Any secret included in a distributed APK can be extracted. The sample `.env.example` contains placeholders only.

## External services and data

- **Gemini via your backend proxy:** user-submitted prompts and, for disease analysis, selected plant images may be sent for AI processing. Configure and disclose your proxy's handling, retention, access controls, and privacy terms before public distribution.
- **Open-Meteo:** weather requests use location coordinates or a user-entered location.
- **OpenStreetMap Nominatim:** coordinates and location-search terms are used for geocoding under its public usage policy.
- **Local storage:** account credentials (salted and hashed), profile, fields, and tasks are stored in the device's Room database. There is no cloud account sync in this project. Uninstalling the app or clearing storage deletes local data.

Review the privacy policy and service terms for current policies before shipping.

## Prerequisites

- Android Studio with a compatible Android SDK.
- JDK required by the project's configured Android Gradle Plugin / Gradle version.
- A deployed, HTTPS-enabled AgriX AI backend proxy for AI features.

## Configuration

1. Copy `.env.example` to `.env` for local configuration.
2. Set `AI_BACKEND_BASE_URL` to your real HTTPS backend URL, including the trailing slash.
3. Do not place the Gemini key in the Android app. Set `GEMINI_API_KEY` only in the backend's server-side secret manager.
4. Keep local `.env`, keystore files, and signing credentials out of Git.

AI calls are intentionally disabled until a valid HTTPS backend is configured. Weather and geocoding do not require the Gemini backend.

## Build and verification

The repository includes Gradle wrapper configuration. Generate/restore the wrapper scripts and JAR for the configured Gradle version if they are not present, then run:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

For a distributable release, configure release signing credentials through environment variables or secure CI secrets, then build and verify a signed release bundle. Never commit the keystore or passwords.

## Privacy and security

Read [SECURITY.md](SECURITY.md) and [PRIVACY.md](PRIVACY.md) before using or distributing the application. The privacy draft must be reviewed and updated with accurate operator/contact details and the actual backend's data practices before release.

## Agricultural safety

AI output is informational decision support and can be incorrect. Disease identification can be uncertain. Consult a qualified local agricultural officer or Krishi Vigyan Kendra before applying pesticides, fungicides, or other chemicals.

## License

No open-source licence is granted until the project owner selects and adds a licence file. Do not assume reuse rights from repository visibility alone.
