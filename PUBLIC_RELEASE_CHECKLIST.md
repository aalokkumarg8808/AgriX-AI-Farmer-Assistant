# Public repository and release checklist

Use this checklist before changing repository visibility or distributing an APK. Public source-code availability and a production-ready mobile release are separate milestones.

## Repository visibility

- [ ] Review the complete Git history for accidentally committed credentials or private data. Rotate any exposed keys; deleting a key from the latest tree is not enough.
- [ ] Confirm that all tracked assets, sample data, names, and documentation are intended for public disclosure.
- [ ] Choose and add an explicit project license. Do not assume code reuse is permitted without one.
- [ ] Add the standard Gradle Wrapper launcher scripts and wrapper JAR, then verify a clean clone can build without a preconfigured Android Studio installation.
- [ ] Run a secret scanner and inspect the findings manually. A clean current tree does not prove that Git history is clean.
- [ ] Set up CI for debug assembly, unit/Robolectric tests, and lint; fix or document failures.

## Mobile app release

- [ ] Build and smoke-test on a real Android device and a current emulator.
- [ ] Confirm runtime permission behavior for camera and location, including denial and “don't ask again” paths.
- [ ] Configure a controlled AI backend that keeps the Gemini API key server-side. Do not ship a key compiled into the APK.
- [ ] Secure the backend with appropriate access controls, quotas, rate limits, and abuse prevention.
- [ ] Publish a user-facing privacy notice that names the data sent to Gemini/the configured proxy, Open-Meteo, and OpenStreetMap Nominatim.
- [ ] Verify Nominatim's current usage policy and make sure the app's identification/contact arrangement meets its requirements.
- [ ] Review all manifest permissions and backup behavior against the data the app stores.
- [ ] Test offline behavior, API errors, rate limits, empty responses, and inaccurate/low-confidence AI results.
- [ ] Use release signing credentials supplied through secure local/CI configuration. Never commit signing keys or passwords.
- [ ] Add genuine, up-to-date screenshots and installation instructions only after testing the displayed build.
- [ ] Review agricultural safety language and make sure the app does not present AI output as a guaranteed diagnosis or treatment.

## Status at this review

- [x] README reorganized and public-facing limitations clarified.
- [x] Local environment, signing, service-account, database, and build artifacts added to the Git ignore rules.
- [x] External service data flows documented.
- [x] Security reporting and contribution guidance added.
- [ ] Full Git-history secret scan not completed in this review.
- [ ] Android build, tests, lint, and device smoke tests not run in this review.
- [ ] License and end-user privacy notice not supplied.
- [ ] Gradle Wrapper launcher scripts/JAR are missing from the current repository tree.
