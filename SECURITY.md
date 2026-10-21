# Security Policy

## Scope

This repository contains the AgriX AI Android application. The `main` branch is the current development line; a supported release policy has not yet been published.

## Reporting a vulnerability

Please **do not** report exploitable vulnerabilities, API keys, personal data, or unredacted logs in a public issue.

Use GitHub's private vulnerability reporting or security advisory feature for this repository if it is enabled. Otherwise, contact the repository owner through [their GitHub profile](https://github.com/aalokkumarg8808) and request a private channel before sharing technical details.

Include the affected revision, impact, reproducible steps, and a proposed mitigation where possible. Redact credentials, access tokens, farmer information, GPS coordinates, and other personal data.

## Credential exposure

If a real API key or password is committed accidentally, revoke or rotate it immediately. Removing it from the latest tree does not remove it from Git history or existing clones. Treat any exposed credential as compromised.

## Security expectations

- Keep Gemini API keys on a controlled server for distributed builds. A key compiled into Android `BuildConfig` or an APK can be extracted.
- Never commit `.env`, `google-services.json` unless it has been reviewed and intentionally approved for publication, signing files, passwords, or real local databases.
- Use restricted API keys and provider quotas for development.
- Before sharing a release build, review permissions, external data transfers, dependency updates, and the end-user privacy notice.
