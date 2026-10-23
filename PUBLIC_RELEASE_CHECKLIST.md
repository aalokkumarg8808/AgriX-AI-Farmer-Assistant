# AgriX AI — Public Release Checklist

Do not mark the application as release-ready until every relevant item is verified.

## Secrets and API security

- [ ] Run a full Git-history scan with a secret scanner (for example, Gitleaks or TruffleHog) on every branch and commit reachable by the public repository.
- [ ] Revoke and rotate any real key or credential ever committed; removing it from the working tree is not sufficient.
- [ ] Configure `AI_BACKEND_BASE_URL` to a real HTTPS backend with a trailing slash.
- [ ] Keep `GEMINI_API_KEY` only in backend secret storage, never in Android client configuration.
- [ ] Test that the Android APK/AAB contains no real key, and that the app fails closed without a valid proxy.
- [ ] Add backend caller controls, quotas, rate limiting, request validation, and cost/abuse monitoring.

## Android data and build

- [ ] Review device/cloud backup behaviour for local credentials and farm data.
- [ ] Restore and verify Gradle wrapper scripts and wrapper JAR against the configured Gradle distribution.
- [ ] Build debug and release variants from a clean checkout.
- [ ] Run unit tests and Android lint; investigate failures.
- [ ] Verify release signing and keep keystore/passwords out of Git.
- [ ] Inspect the generated APK/AAB for secrets and unnecessary permissions/data.
- [ ] Smoke-test registration, login, account switching/deletion, camera/gallery, location denial, offline fallback, and API errors.

## Public project information

- [ ] Complete and review `PRIVACY.md` and publish it at a stable, reachable URL.
- [ ] Choose and add a licence only after deciding the intended reuse rights.
- [ ] Add a monitored security contact or enable GitHub private vulnerability reporting.
- [ ] Verify third-party API usage and attribution/rate-limit requirements.

## Current known limitations

- The repository's Gradle wrapper properties exist, but wrapper scripts and wrapper JAR were not found during this audit; restore them before relying on `./gradlew` commands.
- No GitHub Actions workflow runs were found during audit.
- The privacy notice is a draft with owner-supplied fields; it is not a final policy.
- Full-history secret scanning, Android build/tests/lint, runtime testing, and release-signed APK/AAB inspection still need to be executed.
