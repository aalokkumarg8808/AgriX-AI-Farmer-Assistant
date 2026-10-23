# AgriX AI — Privacy Notice (Draft; owner review required)

**This is a draft, not a final publishable policy. The project owner must complete the operator/contact and backend details below and verify every statement against the shipped build and deployed services before public distribution.**

## Information stored on device

AgriX AI stores local account information, salted and hashed password credentials, farmer profile data, fields, and farm tasks in an on-device Room database. The current project does not provide cloud account synchronisation. Clearing app storage or uninstalling the app removes local data.

## Location and weather

With permission, the app may access approximate or precise device location to determine a location and retrieve weather. Users may also enter a location manually. Location coordinates or search terms may be sent to Open-Meteo and OpenStreetMap Nominatim for weather and geocoding.

## AI processing

When AI features are used, text prompts may be sent to the configured AgriX AI HTTPS backend. Plant-disease analysis may also send a selected plant image. The backend may forward requests to a generative AI provider. Before release, document the deployed backend and provider, data retention, access controls, processing purposes, and deletion practices here.

## Permissions and deletion

The application requests internet access and may request location and camera access for corresponding features. Permissions should be requested in context when the user invokes a feature that needs them. Users can use the application's account deletion flow to remove local account data; uninstalling or clearing app storage also removes the local database.

Password hashing helps protect stored credentials but does not protect against every form of device compromise. The operator must also assess applicable age restrictions and legal requirements for the intended distribution regions.

## Operator information — required before release

- App operator/legal name: **[Owner must complete]**
- Privacy contact email or monitored support channel: **[Owner must complete]**
- Production backend URL and operator: **[Owner must complete]**
- Backend/provider retention, processing, and deletion details: **[Owner must complete]**
- Effective date and applicable jurisdictional disclosures: **[Owner must review]**
