# Task 10 — Post drafts → Room

**Size:** Medium (3–8 h) • **Priority:** P3 • **Status:** Open

## Goal

Migrate post-composer drafts from JSON files (`FilePostDraftRepository`)
to Room, per the stack rules in `.cursor/rules/project.mdc`
("Persistence: DataStore (prefs/search history), files/Room (drafts)").

## Why it matters

The Room dependency is already on the classpath but not used. Current
drafts are stored as JSON files in `filesDir/post_drafts/`. Room gives
us:

- Transactional writes (no partial-write corruption after a crash).
- Query composability if drafts grow (e.g., filter by "has images").
- Migrations for schema evolution.

Behavior stays the same — this is a persistence swap, not a feature
change.

## Contract

### Schema

Single table `post_drafts` (one row per draft):

| Column | Type | Notes |
|--------|------|-------|
| `id` | TEXT (PK) | Draft UUID (generated on first save) |
| `updated_at` | INTEGER (epoch ms) | Sorting key |
| `payload_json` | TEXT | The full `PostDraft` serialized as JSON |

Keep the payload as JSON blob (rather than normalized columns) so the
`PostDraft` model can evolve without a migration. Schema changes to
`PostDraft` fields become **application-level** compatibility, handled
in the serializer.

### Room setup

Create `data/post/db/`:

- `DraftsDatabase.kt` — `@Database(entities = [DraftEntity::class], version = 1)`
- `DraftEntity.kt` — mirrors the schema above.
- `DraftDao.kt` — `getAll()`, `getById(id)`, `upsert(draft)`,
  `deleteById(id)`, `count()`.
- `RoomPostDraftRepository.kt` — implements existing `PostDraftRepository`
  by delegating to the DAO and (de)serializing `PostDraft` via
  `kotlinx.serialization`.

DI:
- `di/PostModule.kt` — provide `DraftsDatabase` (singleton), `DraftDao`,
  and bind `PostDraftRepository` to `RoomPostDraftRepository`.

### Migration from file JSON

On first run after upgrade:

1. On `RoomPostDraftRepository` init, if the file directory
   `filesDir/post_drafts/` exists, read every file, upsert into Room,
   then delete the file (idempotent — safe on subsequent runs).
2. Log via `SpotLogger` under `LogCategory.Post` (no PII).

## iOS reference (for reviewers)

Not applicable — iOS uses its own draft persistence
(`PostDraftStore.swift`). This is an Android-side hardening task per
Android stack rules.

## Android target (files to touch)

Create:
- `data/post/db/DraftsDatabase.kt`
- `data/post/db/DraftEntity.kt`
- `data/post/db/DraftDao.kt`
- `data/post/RoomPostDraftRepository.kt`
- `app/src/test/.../data/post/RoomPostDraftRepositoryTest.kt`
  (using Room in-memory DB).

Edit:
- `di/PostModule.kt` — swap `FilePostDraftRepository` for the Room
  binding.
- `data/post/FilePostDraftRepository.kt` — keep temporarily, mark
  `@Deprecated`, delete after the migration ships in one release.

Preserve:
- `PostDraftRepository` interface signature — **do not change**.
- `PostDraft.kt` model.
- `PostViewModel` and all consumers — they should not need edits.

## Acceptance criteria

- [ ] `PostDraftRepository` binding in Hilt resolves to
      `RoomPostDraftRepository`.
- [ ] Drafts persist across app kill and process death.
- [ ] Existing JSON drafts (if any) are migrated on first run and the
      file directory is emptied.
- [ ] All existing post-flow behavior unchanged (create, edit, save,
      delete draft).
- [ ] `RoomPostDraftRepositoryTest` covers: create, update, delete,
      list, migration from a seeded file directory.
- [ ] `PostViewModelTest` still passes without changes (uses the fake).
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug` green.

## Test plan

- Unit tests as above.
- Manual: on a build with a saved file-based draft, upgrade to the new
  build; confirm the draft appears in the drafts sheet and the file
  directory is empty afterwards.

## Out of scope

- Changing the `PostDraft` schema itself.
- Adding a drafts search UI.
- Cloud sync of drafts.

## Follow-ups

- Delete `FilePostDraftRepository` after one release.
- Consider moving search history off DataStore to Room too — separate
  task if useful.
