# Task 07 — Missing repository fakes for tests

**Size:** Small (1–3 h) • **Priority:** P2 • **Status:** Open

## Goal

Every repository interface must have a fake under `app/src/test/` so
ViewModels are unit-testable without live Supabase. Three are missing.

## Why it matters

The rule `.cursor/rules/project.mdc` says: *"Repositories are interfaces
with fakes so ViewModels are unit-testable with no live Supabase, no real
billing, no real OAuth."* Three interfaces currently lack fakes, which
either blocks unit tests for their consumers or forces those tests to
skip meaningful coverage:

| Interface | Real impl | Fake impl | Impact |
|-----------|-----------|-----------|--------|
| `SettingsRepository` | `SupabaseSettingsRepository` | — | Account/security VM tests can't stub server calls |
| `SpotDetailRepository` | `SupabaseSpotDetailRepository` | — | Deep-link overlay tests can't run |
| `FollowingIdsRepository` | `SupabaseFollowingIdsRepository` | — | Map "Following" filter tests can't run |

## Contract

For each missing fake:

1. Look at the interface's method signatures.
2. Implement each method with an in-memory backing store (a `MutableStateFlow`
   or `mutableListOf` field) and a per-method behavior override:
   - Default: return a canned success.
   - Test can override via a `var nextResult: Result<T> = Result.success(...)`
     or a lambda field for per-call control.
3. Match the pattern already used by, for example,
   `FakeFeedRepository`, `FakeAuthRepository`, `FakeSafetyRepository`.

### Skeleton

```kotlin
class FakeSettingsRepository : SettingsRepository {
    var updatePasswordResult: Result<Unit> = Result.success(Unit)
    var deleteAccountResult: Result<Unit> = Result.success(Unit)

    override suspend fun updatePassword(current: String, new: String): Result<Unit> =
        updatePasswordResult

    override suspend fun deleteAccount(): Result<Unit> =
        deleteAccountResult

    // ...one field per method
}
```

Do the same for the other two.

### Test coverage add-ons

- `AccountSettingsViewModelTest` — add error-path cases for
  `updatePassword` and `deleteAccount`.
- `SecuritySettingsViewModelTest` — add a case for privacy-toggle failure.
- `MapViewModelTest` — add a case for the Following filter with a
  seeded `FakeFollowingIdsRepository`.
- A new `SpotDetailOverlayFlowTest` (unit or instrumented) — deep-link
  loading → success and → unavailable.

## iOS reference (for reviewers)

- The iOS app uses XCTest with protocol-based fakes, e.g.
  `../spot-ios-app/SpotTests/**` — same architectural idea.

## Android target (files to touch)

Create:
- `app/src/test/java/com/spot/android/data/settings/FakeSettingsRepository.kt`
- `app/src/test/java/com/spot/android/data/deeplink/FakeSpotDetailRepository.kt`
- `app/src/test/java/com/spot/android/data/map/FakeFollowingIdsRepository.kt`

Optional new tests:
- Add or extend `AccountSettingsViewModelTest`, `SecuritySettingsViewModelTest`,
  `MapViewModelTest`.

## Acceptance criteria

- [ ] Each of the three fakes exists and compiles.
- [ ] Each fake supports at minimum: seed success data, force error on
      next call, observe interactions (e.g., `lastPasswordUpdate`).
- [ ] At least one new/existing ViewModel test consumes each fake.
- [ ] `./gradlew testDebugUnitTest` passes with new tests.

## Test plan

- `./gradlew testDebugUnitTest`.

## Out of scope

- Refactoring the real Supabase impls.
- Renaming any interfaces.
- Adding fakes for non-repository classes (`ImageProcessor`, etc.) —
  only repositories.

## Follow-ups

- If a repository grows a new method later, updating the fake is part
  of that PR's scope. Don't leave fake gaps.
