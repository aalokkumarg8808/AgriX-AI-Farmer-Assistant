# Contributing to AgriX AI

Thanks for helping improve AgriX AI.

## Before opening an issue

- Search existing issues to avoid duplicates.
- Include concise reproduction steps and the expected versus actual behavior.
- Remove API keys, access tokens, GPS coordinates, personal information, and private farm data from screenshots and logs.

## Development setup

1. Install Android Studio, the required Android SDK components, and a compatible JDK.
2. Clone the repository and open it in Android Studio.
3. Let Gradle sync finish before editing.
4. Copy `.env.example` to `.env` only if the feature you are testing needs local configuration. Keep real credentials out of Git.

The repository currently does not include the Gradle wrapper launcher scripts/JAR. Use Android Studio's Gradle integration or a locally installed compatible Gradle version until the wrapper is restored.

## Making a change

1. Create a focused branch from `main`.
2. Keep changes limited to one feature or fix where practical.
3. Follow the existing Kotlin and Compose style.
4. Add or update tests for changed behavior when practical.
5. Update the README or service documentation when behavior, setup, permissions, or external data flows change.

## Verification

Before opening a pull request, run the checks available in your environment:

```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
gradle :app:lintDebug
```

These commands require a compatible local Gradle installation and Android SDK. If a check cannot be run, say so in the pull request rather than claiming it passed.

## Pull requests

A useful pull request includes:
- What changed and why.
- Relevant screenshots for user-interface changes, with personal data removed.
- Tests and build checks performed, including any that could not be run.
- Any configuration or migration steps required.

Do not add real secrets, signing credentials, generated APKs, real user databases, or unlicensed assets.
