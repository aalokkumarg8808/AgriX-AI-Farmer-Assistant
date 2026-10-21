# External services and data flow

AgriX AI uses the providers below for online features. Provider availability, terms, pricing, and rate limits can change; verify current provider documentation before a public or commercial release.

## Open-Meteo — weather

- Base endpoint used by the app: `https://api.open-meteo.com/v1/forecast`
- Purpose: current weather metrics and forecast data.
- Requests may include coordinates for the selected or current location.
- No API key is currently configured in the app's weather client.

Provider terms: https://open-meteo.com/en/terms

## Google Gemini — AI assistant, crop suggestions, and image analysis

- The client uses the `gemini-3.1-flash-lite` model through the Generative Language API contract.
- Requests can contain farming questions, conversation context, farm inputs, or a selected plant image.
- Direct mode sends requests to `https://generativelanguage.googleapis.com/` with the API key supplied to the client at build time.
- Proxy mode sends requests to the configured `AI_BACKEND_BASE_URL` and expects a compatible `/v1beta/models/{model}:generateContent` endpoint. The backend must hold the Gemini key server-side.

**Security:** Android `BuildConfig` and APK contents are inspectable. Do not ship an APK that contains a real Gemini API key. This repository does not include a backend proxy implementation or deployment configuration.

Provider documentation: https://ai.google.dev/gemini-api/docs

## OpenStreetMap Nominatim — geocoding and place search

- Base endpoint: `https://nominatim.openstreetmap.org/`
- Purpose: reverse geocoding coordinates and searching location names.
- Requests may contain latitude/longitude or a text query.
- The client caches successful results in memory and rate-limits requests within the app process.
- The current request User-Agent identifies the app and links to this repository. Re-check the provider's current usage policy and contact-information requirements before public deployment.

Usage policy: https://operations.osmfoundation.org/policies/nominatim/

## Local data

The app stores local account/profile and farm-management data in the Room/SQLite database on the device. That does not mean all app activity stays local: location lookups go to Nominatim, weather requests can include coordinates, and AI prompts or selected images go to the configured AI provider or proxy.

Before distribution, publish a user-facing privacy notice explaining data sent to third parties, retention, controls, and relevant provider policies. Do not use real farmer data during testing unless the data handling has been reviewed.
