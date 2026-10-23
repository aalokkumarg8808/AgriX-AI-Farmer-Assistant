# Security Policy

## Reporting a vulnerability

Do not open a public issue containing secrets, credentials, personal data, or exploitable details. Use GitHub's private vulnerability reporting feature if it is enabled. Before public release, the maintainer should enable private reporting or add a monitored security contact.

## Key and secret handling

- Never commit `.env`, API keys, access tokens, keystores, signing passwords, private keys, or real user data.
- Values packaged in Android `BuildConfig`, resources, or an APK should be treated as public and extractable.
- AgriX AI's Android client deliberately disables direct Gemini API-key use. Configure an HTTPS backend using `AI_BACKEND_BASE_URL`; keep `GEMINI_API_KEY` only in backend-side secret storage.
- Protect the backend with caller authentication or an appropriate access-control strategy, quotas/rate limits, request validation, output limits, and abuse monitoring.
- If a credential was ever committed, revoke/rotate it. Deleting it from the latest files is not enough to remove it from Git history.
- Before making this repository public, run a full-history secret scan. Resolve every true positive, and do not copy secret values into issues, logs, or audit reports.

## Configuration behaviour

When an HTTPS backend URL is absent or invalid, AI features are intentionally left unconfigured rather than falling back to a client-side key. This is fail-closed behaviour.
