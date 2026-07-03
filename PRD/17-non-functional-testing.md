# 17 — Non-Functional Requirements, Analytics, Testing & Release

## Performance

- **Cold start** to first usable screen: keep the splash short (~1.5s) and restore the session in parallel.
- **Feed scrolling** must be smooth (60fps) with image-heavy cards: use Coil with memory+disk caching, downsample to display size, and prefetch next-page images.
- **Map**: cap visible pins at 250, debounce viewport fetches (~250ms), and reuse marker composables. Clear image/map caches on memory pressure (mirror the iOS memory-warning handler).
- **Uploads**: downscale images to ≤1600px before upload; publish runs in the background with a 90s timeout and a progress banner.
- **Signed URLs**: cache signed spot-image URLs until near their 7-day expiry to avoid re-signing on every render.

## Offline & resilience

- Preserve feed content on refresh errors (show a toast, don't clear).
- Preserve drafts locally on publish failure.
- Treat 401/403/empty results as expected when unauthenticated/unauthorized; route to auth where appropriate.
- Retry publish/moderation with clear user messaging; never silently drop a post.

## Security & privacy

- **RLS is authoritative** — never rely on client checks for authorization.
- Never embed the **service-role key**; only the anon/publishable key ships in the client.
- Never log PII, tokens, or raw image bytes.
- Auth gating must run before any sensitive network/DB/posting action.
- Set `platform='android'` on `reports` and `user_terms_acceptances`.
- Store session/tokens in app-private encrypted storage (supabase-kt default).

## Logging

- Structured logger with per-area categories (feed, auth, network, post, map, deep-link, privacy, spot-card) toggleable from a debug settings screen (mirror `SpotLogger` + the debug log keys in [11-settings.md](11-settings.md)).
- Debug categories off by default in release; a master "log all" toggle for debugging.

## Analytics events (mirror iOS `Constants.Analytics`)

Track at least: `AuthReinstall`, `Perms.Requested`, `Feed.DropPrivate`, `Image.LoadFailed`, `Auth.EmailInUse`, `Auth.DeleteByEmail`, plus deep-link events (`trackDeepLink` with origin + route). Use Firebase Analytics (or your chosen provider). Crash reporting via Crashlytics (optional but recommended).

## Accessibility

- Content descriptions on all interactive controls.
- Sufficient contrast (the cream/green palette passes for text; never render cream-on-cream).
- Respect system font scaling; test large-font layouts on cards and the composer.
- Reuse stable test tags matching iOS identifiers where practical (`navigation.homeTab`, `home.feedRoot`, `welcome.screen`, etc.).

## Testing strategy

Mirror the iOS split (unit vs UI) and the project testing rules:

- **Unit tests** (JVM, ViewModels + repositories with fakes): feed load states + empty reasons, like/bookmark optimistic + rollback, bookmark cap (50) → paywall, post limits (1/5 images & vibes), vibe validation (2–30 chars), map drawer dismiss policy, deep-link parsing + spot-id validation, follow/private-account gating, report/block flows, Pro entitlement resolution + account binding, feed-diversity reordering.
  - Use **fakes/mocks** — no live Supabase, no real billing prompts, no real OAuth in unit tests.
- **Instrumented/UI tests** (Compose + Espresso): stable flows — launch→welcome→auth gate, tab navigation, feed scroll + like, post composer happy path (mocked publish), settings→delete-account gating, paywall presentation. Prefer test tags over localized strings.
- **RLS/data tests**: validate private-account visibility and block filtering against a test Supabase context where feasible.
- Do not weaken production code just to satisfy tests; use dependency injection / test hooks.

### High-value test scenarios (parity-critical)

| Area | Must verify |
|------|-------------|
| Safety | Terms gate before registration (email + OAuth); report + block reachable; moderation blocks unsafe images |
| Limits | 1/5 images, 1/5 vibes, 50 bookmark cap, 2–30 vibe length |
| Feed | 24 page size; empty-state copy per status; seen-fallback; optimistic like/bookmark |
| Map | 250 cap; drawer dismiss reasons; Pro-only filters hidden for free |
| Deep links | `/s/{id}` parsing; invalid id rejected; pending link resolves after login |
| Pro | gates match the matrix; entitlement writes `is_pro`/`pro_until`; wrong-account binding denies Pro |
| Account | delete account with re-auth; logout clears session |

## Release checklist (Android)

- [ ] `assetlinks.json` live on `spotapp.online` + `www` with the release signing cert SHA-256; App Links verify.
- [ ] Google Play subscription product created; entitlement verified server-side; restore works.
- [ ] Terms/Privacy URLs + support email match store listing (`https://spotapp.online/terms`, `/privacy`, `support@spotapp.online`).
- [ ] Account deletion reachable and functional (`delete_my_account`).
- [ ] All five UGC safety guarantees demonstrable (see [13-moderation-safety.md](13-moderation-safety.md)).
- [ ] Anon key (not service key) in the shipped build; RLS verified.
- [ ] Notification permission requested post-onboarding; `POST_NOTIFICATIONS` handled on 13+.
- [ ] Smoke test one real spot deep link on a physical device.
- [ ] Crash/analytics wired; no PII in logs.

## Data-plane guardrail

Supabase is the **only** data plane for users, spots, media, and social data. Do not add a second upload path or a parallel datastore (Firestore/Firebase Storage) for app content — Firebase is analytics/crash only. This mirrors the iOS `DataPlaneGuardTests` rule.
