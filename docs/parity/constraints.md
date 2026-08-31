# Constraints (non-negotiable)

These rules apply to **every** parity task. A PR that violates any of them
will be rejected regardless of how much code it ships.

> These are distilled from `.cursor/rules/project.mdc` and the iOS shipping
> contract. Read `.cursor/rules/project.mdc` too — it is the authoritative
> version if these ever drift.

## 1. Backend & data plane

- **Same Supabase project as iOS:** `aeurigbbohyxvtsfiyul`. Never fork the
  schema. Never write a second migration path. If a table or RPC doesn't
  already exist, the answer is not "add it" — the answer is "read the iOS
  contract in [`data-contracts.md`](data-contracts.md) and use what's there."
- **RLS is authoritative.** Never rely on client-only authorization. Treat
  401/403 and empty RLS-filtered results as expected outcomes when the user
  is unauthenticated or unauthorized. Handle them explicitly in UI state.
- **Only the anon (publishable) key ships in the client.** Never embed the
  service-role key. Never invoke privileged Supabase Admin APIs from Android.
- **Supabase is the only data plane** for users, spots, media, and social
  data. Firebase = analytics + crash + App Check + FCM only. No parallel
  datastore. No parallel upload path.

## 2. Media & moderation

- **Every uploaded image must pass moderation.** Upload to `pending_images`,
  invoke the `moderate-image` edge function, and only then reference the
  approved asset via the publish/edit RPC. No client-only bypass.
- **Downscale images to ≤1600px longest edge, JPEG ~0.8** before upload
  (see `data/post/ImageProcessor.kt`).
- **Signed URLs cached until near their 7-day expiry** — reuse
  `core/media/ImageUrlSigner.kt`. Never call `createSignedUrl` on every render.

## 3. Safety features (v1, non-negotiable)

- **Terms agreement before registration.** Signup blocked until the checkbox
  is on and the flag is persisted via `PreAuthTermsAgreementStore`.
- **Image + text moderation** on every publish and edit.
- **Report content** (spots and profiles) via `submit_content_report` RPC.
- **Report + block users** via `block_user_v1` RPC.
- **Content filtering** based on blocks and suspensions — feed and search
  results must exclude blocked authors and hidden content.
- **`platform = "android"`** must be set on `reports` and
  `user_terms_acceptances` writes (iOS omits this; Android must add it).

## 4. Limits (must match iOS exactly)

Pull the value from [`limits-constants.md`](limits-constants.md) or from
`core/util/Constants.kt`. Never inline a magic number in feature code.

| Concern | Value |
|---------|-------|
| Feed page size | 24 |
| Free post images / vibes | 1 / 1 |
| Pro post images / vibes | 5 / 5 |
| Free bookmark cap | 50 (server-enforced; paywall client-side) |
| Vibe tag length | 2–30 chars |
| Publish timeout | 90 s |
| OTP length | 6 digits |
| OTP resend cooldown | 30 s |
| Map visible spots cap | 250 |
| Search debounce | 300 ms |
| Signed URL TTL | 7 days |
| Report details max | 500 chars |
| Image max long edge | 1600 px |

## 5. Auth

- **Email + password + email OTP** is the primary flow.
- **Sign in with Apple → Google Sign-In** on Android (via supabase-kt OAuth,
  callback `spotapp://auth-callback`). This is the sanctioned platform swap;
  do not re-debate.
- **Post-auth username gate** runs for any OAuth account without a username
  (`sync_current_user_v1` will surface the missing username; route to
  `UsernameSetupScreen` before letting the user into the tab shell).

## 6. Architecture

- **Layering is strict:** UI (Compose) → ViewModel → Repository/Service
  (typed interfaces) → Supabase.
- **No business logic in composables.**
- **No Supabase calls from composables or ViewModels directly.** All access
  goes through a repository interface. This includes debug screens — the
  current `AlgorithmSnapshotViewModel` violates this and is task 05.
- **Repositories are interfaces with fakes** so ViewModels can be unit-tested
  without a live Supabase, real billing, or real OAuth. If you add a real
  impl, add a fake in `app/src/test/java/.../data/**/Fake<Name>Repository.kt`.
- Package layout: `core/`, `data/`, `di/`, `feature/`, `navigation/`. Do not
  invent a new top-level structure.
- **Shared session state** — the current user's liked / bookmarked / blocked /
  Pro sets live in `UserSessionHolder`. Feed, map, search, profile all read
  from it. Never duplicate that state in a feature.

## 7. DTOs

- DTOs use **snake_case field names** matching Postgres. No global
  snake_case → camelCase conversion.
- Domain models (`Spot`, `User`, `VibeTag`, …) are assembled from DTOs in
  `data/mapper/`.
- Postgres → Kotlin: `uuid` → `String`, `double precision` → `Double`,
  `timestamptz` → ISO-8601 `String`, `uuid[]` → `List<String>`, `jsonb` →
  `JsonObject`.
- Profile writes go through `sync_current_user_v1` — **never** a direct
  upsert on `users`.

## 8. Design system

- **Light theme only** (no dark mode in v1 — matches iOS).
- Every color, spacing, radius, and typography value comes from the central
  theme in `core/design/theme/` and `core/design/Dimensions.kt`.
- **No inline hex in feature code.** Use `SpotColors.*` or
  `MaterialTheme.colorScheme.*`.
- Reuse shared components: `SpotCard`, `Avatar`, `VibeChip`, `SkeletonSpotCard`,
  `EmptyFeedView`, `Toast`, `TopNavigationView`, `PermissionPrePrompt`,
  `PaywallSheet`. Do not re-implement these per screen.

## 9. Test tags

- Use `Modifier.testTag(...)` with the **iOS accessibility identifier
  strings** so the shared UI-test vocabulary works.
- Common namespaces already in use: `navigation.homeTab`, `home.feedRoot`,
  `home.spotCard.*`, `map.mapRoot`, `map.filter.*`, `post.step.*`,
  `search.segment.*`, `profile.spotGrid`, `settings.*`, `paywall.*`,
  `safety.reportSheet.*`, `welcome.screen`, `auth.login.*`, `signUp.*`,
  `confirmEmail.*`, `usernameSetup.*`, `termsUpdate.*`, `onboarding.coach*`.
- If a new test tag is needed, mirror the iOS identifier string. Check
  [`screen-map.md`](screen-map.md) for the iOS name.

## 10. Logging & PII

- **Never log PII, tokens, or raw image bytes.**
- Use `SpotLogger` with the per-area categories in `core/logging/`.
- Debug-only detail (profile ≥ 3) is fine; profile 1 must be quiet.

## 11. UI states — every user-facing surface handles all four

1. **Loading** (skeleton or spinner)
2. **Empty** (with actionable copy — see iOS `EmptyFeedView`)
3. **Error** (toast or inline banner)
4. **Unauthenticated** (block sensitive actions behind an auth gate)

If your feature can be reached while signed out, the ViewModel must handle
the null session gracefully. It's not the composable's job.

## 12. Optimistic mutations

- Like, bookmark, and delete are **optimistic**: update local state
  immediately, roll back on failure, show an error toast.
- Publish is **background-queued** with a 90 s hard timeout; use
  `SpotPublishCoordinator` and its banner state.

## 13. Feed events

- Emit feed events via `FeedEventService` (coalesced `record_feed_event_v1`).
  Do not fire per-scroll writes.

## 14. Gating flows

Every sensitive network / DB / posting action must run **after** these checks:

1. Session exists (`AuthUiState.isAuthenticated`).
2. Email verified (`user.email_verified` — post flow blocks on this).
3. Terms accepted (for signup; not currently gated on session for existing
   users because iOS has `isTermsUpdateGateEnabled = false`).

## 15. When in doubt

- The iOS Swift file is the source of truth for behavior, copy, and states.
  Path: `../spot-ios-app/Spot/`.
- The live Supabase schema is the source of truth for contracts. If a
  contract in [`data-contracts.md`](data-contracts.md) looks wrong, check
  the actual Supabase project before implementing.
- Anything marked **[ANDROID DECISION]** in a task file already has a
  recommended default; follow it unless there's a stated reason not to.

## 16. Definition of done (every parity task)

- ✅ Matches the iOS behavior, copy, states, and constants.
- ✅ Loading / empty / error / unauthenticated states all handled.
- ✅ Unit tests for ViewModel logic using fakes (`app/src/test/...`).
- ✅ Test tags match iOS identifier strings where practical.
- ✅ `./gradlew assembleDebug lintDebug testDebugUnitTest` all green.
- ✅ No hardcoded secrets; anon key only; RLS relied on for authz.
- ✅ No new PII in logs.
- ✅ No inline hex colors, no direct Supabase calls from composables or
  ViewModels, no new top-level packages.
