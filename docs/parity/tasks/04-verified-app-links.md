# Task 04 — Verified App Links (`assetlinks.json`)

**Size:** Small (1–3 h; mostly ops) • **Priority:** P1 • **Status:** Open

## Goal

Make `https://spotapp.online/s/<spotId>` links open **directly in the
Spot app on Android** without a chooser, matching iOS Universal Links.

## Why it matters

The manifest already declares:

```
<intent-filter android:autoVerify="true">
  <data android:scheme="https" android:host="spotapp.online" ... />
</intent-filter>
```

but Android requires a hosted `assetlinks.json` at
`https://spotapp.online/.well-known/assetlinks.json` for the verification
to succeed. Without it, users see a chooser (or the link opens in a
browser), breaking share-link parity with iOS.

## Contract

### File shape

Serve at `https://spotapp.online/.well-known/assetlinks.json` **and**
`https://www.spotapp.online/.well-known/assetlinks.json` (both hosts
listed in the manifest filter):

```json
[
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "com.spot.android",
      "sha256_cert_fingerprints": [
        "<UPPERCASE:COLON:SEPARATED:DEBUG:SHA256>",
        "<UPPERCASE:COLON:SEPARATED:RELEASE:SHA256>"
      ]
    }
  }
]
```

Requirements:
- Served over HTTPS with a valid cert.
- `Content-Type: application/json`.
- **No** redirect (301/302 will fail verification).
- File must be reachable while unauthenticated (public).

### Where to get the SHA-256 fingerprints

**Debug:**
```bash
keytool -list -v -alias androiddebugkey \
  -keystore ~/.android/debug.keystore \
  -storepass android -keypass android
```
Copy the `SHA-256` value.

**Release:** From the Play Console → App integrity → App signing → App
signing key certificate → SHA-256 fingerprint (Play manages this key).

Both fingerprints must be listed together — verification succeeds on
either.

### Verification

After deploying:

```bash
adb shell pm verify-app-links --re-verify com.spot.android
adb shell pm get-app-links com.spot.android
```

Expected output: `Domain verification state: verified` for both
`spotapp.online` and `www.spotapp.online`.

Also test manually: send yourself a `https://spotapp.online/s/abc123`
link in a chat app; tapping it should open Spot without a chooser.

## iOS reference (for reviewers)

- `../spot-ios-app/Spot/Spot.entitlements` — associated domains
  `applinks:spotapp.online`.
- `../spot-ios-app/Spot/Info.plist` — URL scheme, universal link config.
- `apple-app-site-association` is the iOS equivalent of `assetlinks.json`
  (already hosted by iOS team). This task adds the Android analog.

## Android target (files to touch)

Mostly non-code:

- Host the `assetlinks.json` file (server-side change, coordinate with
  whoever owns `spotapp.online`).
- Add a note in `docs/parity/tasks/04-verified-app-links.md` (this file)
  with the actual fingerprints once deployed (redact if sensitive).

Code (optional, small):

- `AndroidManifest.xml` — confirm `autoVerify="true"` on the App Link
  filter (already present, but re-verify).
- `app/build.gradle.kts` — add a comment linking to this doc so the next
  release engineer knows where the fingerprints came from.

## Acceptance criteria

- [ ] `assetlinks.json` returns 200 at both
      `https://spotapp.online/.well-known/assetlinks.json` and the `www.`
      variant.
- [ ] `curl -sI https://spotapp.online/.well-known/assetlinks.json`
      shows `content-type: application/json` and no redirect.
- [ ] `adb shell pm get-app-links com.spot.android` shows both domains
      as `verified` on a fresh install.
- [ ] Tapping `https://spotapp.online/s/<realSpotId>` from another app
      opens Spot directly (no chooser).
- [ ] Tapping `https://www.spotapp.online/s/<realSpotId>` also opens
      Spot directly.
- [ ] Test failure path: tap an invalid spot ID; overlay shows
      `SpotUnavailableOverlay`.

## Test plan

- Verify with a debug build first (add the debug SHA-256).
- Cut a Play internal-track release with the release SHA-256; verify on
  a Play-installed build (not sideloaded — sideloaded APKs use the
  debug key on many devices).

## Out of scope

- Adding new deep-link routes.
- Changing the `spotapp://` custom scheme.
- Universal Links / iOS-side changes.

## Follow-ups

- After first successful verification, add a monitoring check that pings
  the `assetlinks.json` URL daily so silent 404s don't break links.
- If we roll out release-key rotation, both fingerprints must appear in
  the file for at least one release window before removing the old one.
