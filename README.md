# AgriX AI — Smart Farming Assistant

AgriX AI is an Android app prototype designed to help farmers access local weather, manage farm information, explore crop recommendations, analyze plant-leaf images, and ask farming questions in multiple languages.

The app is built with **Kotlin**, **Jetpack Compose**, **Room (SQLite)**, **Retrofit**, and **OkHttp**. It is an ongoing project—not a substitute for qualified agricultural advice.

## Features

- **AI farming assistant** — answers farming questions in English, Hindi, and Hinglish when an AI endpoint is configured.
- **Plant disease analysis** — accepts a photo from the system photo picker or camera flow and displays an AI-generated result when the AI service is available.
- **Crop recommendations** — combines farm inputs with AI recommendations when configured, and provides a local rule-based fallback in supported cases.
- **Weather and forecast** — displays weather information and a five-day forecast using Open-Meteo.
- **Farm management** — keeps local user profiles, fields, and tasks in a Room/SQLite database.
- **Location search** — uses OpenStreetMap Nominatim for reverse geocoding and place search.
- **Multilingual interface** — localization strings are provided for several languages, including English, Hindi, Hinglish, and Punjabi.

Availability and accuracy depend on device permissions, network access, provider availability, and configuration. AI-generated outputs can be incomplete or incorrect.

## Technology

- Kotlin and Android
- Jetpack Compose / Material 3
- Room / SQLite
- Retrofit, Moshi, OkHttp, and Kotlin Coroutines
- Google Gemini API (direct development mode or a compatible backend proxy)
- Open-Meteo weather API
- OpenStreetMap Nominatim geocoding API

## Project structure

```text
.
├── app/
│   ├── src/main/java/com/example/
│   │   ├── data/api/          # AI, weather, and geocoding clients
│   │   ├── data/db/           # Room database and DAOs
│   │   ├── data/location/     # Location handling
│   │   ├── data/model/        # Data models and password utilities
│   │   ├── data/repository/   # Data access and app logic
│   │   └── ui/                # Compose screens, components, theme, ViewModel
│   └── src/test/              # Unit and Robolectric tests
├── .env.example               # Safe configuration template (no real API key)
├── SERVICES_MANIFEST.md       # External services and data-flow notes
└── README.md
```

## Getting started

### Requirements

- Android Studio with support for the Android Gradle Plugin version configured in this repository.
- JDK version required by the configured Gradle/Android Gradle Plugin (JDK 17 or newer is a practical starting point).
- Android SDK platform and build tools matching the app's Gradle configuration.
- Internet access for Gradle dependency downloads and online app features.

### Open and build

1. Clone or download this repository.
2. Open the repository folder in Android Studio.
3. Install any SDK components requested by Android Studio and allow Gradle sync to complete.
4. Select the `app` run configuration and run it on an emulator or Android device.

The current repository includes `gradle/wrapper/gradle-wrapper.properties`, but does **not** include the standard `gradlew`, `gradlew.bat`, or wrapper JAR files. Until the Gradle wrapper is added, use Android Studio's Gradle integration or a locally installed compatible Gradle version. With a compatible Gradle installation, these tasks are available:

```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
gradle :app:lintDebug
```

These commands have not been run as part of this documentation update. Run them in your development environment before describing a commit or release as build-verified.

## AI configuration and API-key safety

The project reads local configuration through the Secrets Gradle Plugin. Start with the template:

**macOS / Linux**
```bash
cp .env.example .env
```

**Windows PowerShell**
```powershell
Copy-Item .env.example .env
```

The template contains placeholders only. Never commit your real `.env` file or paste an API key into source code, screenshots, issue reports, or pull requests.

### Recommended mode for any distributed build

Configure `AI_BACKEND_BASE_URL` to point to a backend you control. That backend must implement the endpoint and request/response contract expected by `GeminiApiService.kt` and keep the Gemini API key in server-side environment variables. **This repository does not include or deploy that backend.** The proxy URL is configuration, not a security control by itself; secure the backend with appropriate abuse prevention, quota limits, and access controls.

### Direct key mode — development only

The app supports direct calls to Gemini for local development when no backend URL is configured. A key put into `.env` is compiled into Android `BuildConfig` and can be extracted from the APK. Use only a restricted, disposable key for local testing; do not distribute APKs built with a client-side Gemini key. Rotate a key immediately if it is accidentally exposed.

## Data and privacy notes

- Local account profiles, farm fields, and tasks are stored on-device in Room/SQLite. This is **local app authentication**, not a cloud identity system; accounts do not automatically sync between devices.
- Weather requests may include the selected or current coordinates.
- Location search and reverse geocoding send location/query information to OpenStreetMap Nominatim.
- AI questions and selected plant images are sent to the configured AI endpoint (direct Gemini in development mode or your configured proxy).
- Review the applicable provider terms and privacy policies before using real farmer or farm data. Avoid sending personal or sensitive information unless you have a suitable privacy notice and lawful basis.
- This repository does not currently include a formal end-user privacy policy. Add one before distributing the app publicly.

See [SERVICES_MANIFEST.md](SERVICES_MANIFEST.md) for provider and configuration notes.

## Public-readiness checklist

Before changing repository visibility or distributing an APK, review [PUBLIC_RELEASE_CHECKLIST.md](PUBLIC_RELEASE_CHECKLIST.md). It tracks items that still require an explicit owner decision or build/device verification.

## Security

Please do not publish API keys, keystores, signing passwords, real user databases, or private farmer information. The `.gitignore` is intended to exclude common local secrets and build artifacts, but it cannot remove a secret that was committed in an earlier revision.

If a credential has ever been committed, **revoke or rotate it first**. Removing it from the latest commit is not sufficient to remove it from Git history. See [SECURITY.md](SECURITY.md) before reporting a suspected vulnerability.

## Agricultural safety

AgriX AI is an informational decision-support tool. Crop suggestions, weather data, and AI disease analysis can be inaccurate or incomplete and may not reflect local conditions. Do not apply pesticides, fungicides, or fertilizers solely on the basis of an AI answer. Follow product labels and local rules, and consult a qualified agricultural officer or Krishi Vigyan Kendra (KVK) when needed.

## Contributing

Bug reports and pull requests are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md), avoid including secrets or personal data, and describe any tests you ran.

## License

**No license file is currently included.** Until the project owner adds a license, do not assume that the code may be reused, redistributed, or incorporated into another project. Choose and add a license before presenting the repository as an open-source release.
