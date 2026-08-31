# Task 05 — AlgorithmSnapshot ViewModel → repository

**Size:** Small (1–3 h) • **Priority:** P2 • **Status:** Open

## Goal

Move the direct Supabase query in `AlgorithmSnapshotViewModel` behind a
typed repository, restoring the layering rule "no Supabase calls from
ViewModels."

## Why it matters

`feature/settings/AlgorithmSnapshotViewModel.kt` currently calls
`supabaseClientProvider.client.from("user_feed_profiles")...` directly
(around line 45–50 per the inventory). That violates
`.cursor/rules/project.mdc` and `constraints.md` — every other feature
routes through a repository interface with a fake for tests.

## Contract

### Behavior (unchanged)

- Debug-only screen accessible from Settings → Debug → Algorithm snapshot.
- Loads the current user's `user_feed_profiles` row (their ranking
  profile snapshot).
- Displays the fields (vibe weights, followed-authors weight,
  last-recomputed-at, etc.) as read-only.
- Has a **Recompute** button that calls `recompute_my_feed_profile_v1`
  and reloads.

### Layering fix

Introduce `AlgorithmSnapshotRepository`:

```kotlin
interface AlgorithmSnapshotRepository {
    suspend fun getMySnapshot(): Result<UserFeedProfileDto>
    suspend fun recomputeMySnapshot(): Result<UserFeedProfileDto>
}
```

- Real impl: `SupabaseAlgorithmSnapshotRepository` under `data/settings/`.
- Fake impl: `FakeAlgorithmSnapshotRepository` under
  `app/src/test/.../data/settings/`.
- Add DTO `UserFeedProfileDto` under `data/dto/` with **snake_case**
  fields matching the `user_feed_profiles` table:
  - `user_id: String`
  - `vibe_weights: JsonObject`
  - `followed_weight: Double?`
  - `updated_at: String`
  - …any other columns the current ViewModel reads. Include only what's
    needed for display.

### DI

- Add `AlgorithmSnapshotRepository` binding in `di/SettingsModule.kt`
  (or a new `di/DebugModule.kt` if `SettingsModule.kt` gets crowded).

### ViewModel refactor

- Constructor-inject `AlgorithmSnapshotRepository`.
- Remove all `SupabaseClientProvider` usage from the ViewModel.
- Preserve the existing UI state contract.

## iOS reference (for reviewers)

- `../spot-ios-app/Spot/Views/Profile/AlgorithmDebugView.swift`
- `../spot-ios-app/Spot/ViewModels/FeedProfileViewModel.swift`
- `../spot-ios-app/Spot/Services/Feed/FeedAPI.swift` (contains
  `recompute_my_feed_profile_v1` caller).

## Android target (files to touch)

Create:
- `data/settings/AlgorithmSnapshotRepository.kt`
- `data/settings/SupabaseAlgorithmSnapshotRepository.kt`
- `data/dto/UserFeedProfileDto.kt`
- `app/src/test/.../data/settings/FakeAlgorithmSnapshotRepository.kt`
- `app/src/test/.../feature/settings/AlgorithmSnapshotViewModelTest.kt`

Edit:
- `feature/settings/AlgorithmSnapshotViewModel.kt` — swap the direct
  Supabase call for the repository.
- `di/SettingsModule.kt` — add the binding.

## Acceptance criteria

- [ ] No `SupabaseClientProvider` import in
      `AlgorithmSnapshotViewModel.kt`.
- [ ] `AlgorithmSnapshotRepository` real impl uses supabase-kt.
- [ ] Fake impl allows deterministic tests.
- [ ] `AlgorithmSnapshotViewModelTest` covers: loading, loaded, error,
      recompute-success, recompute-error.
- [ ] UI behavior unchanged (visual regression not expected).
- [ ] `./gradlew testDebugUnitTest lintDebug` green.

## Test plan

- `./gradlew testDebugUnitTest`
- Manual: open Settings → Debug → Algorithm snapshot, verify loading and
  recompute both work against staging.

## Out of scope

- Adding new fields to the snapshot display.
- Making the screen visible in Release (still Debug-only).

## Follow-ups

- If any other ViewModel is discovered doing the same thing, open a
  follow-up task rather than expanding this PR.
