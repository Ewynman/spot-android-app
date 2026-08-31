# Task 19 — Create-a-Spot photo workflow v2

**Size:** Medium (3–8 h) • **Priority:** P2 • **Status:** Open
**iOS reference PR:** [Ewynman/spot-ios-app #71](https://github.com/Ewynman/spot-ios-app/pull/71)

## Goal

Rebuild the post-composer photo workflow around **draft schema v2**:
stable per-image IDs, ordered originals + previews, edit instructions
kept separate, EXIF orientation normalized on gallery import, camera
images normalized before preview/upload, and video entry blocked at
every layer.

## Why it matters

Today drafts store an array of image URIs. Reordering + editing
mid-flow can lose EXIF orientation or blow up when a source file has
been deleted. iOS PR #71 hardens this end-to-end.

## Contract

### Draft schema v2 (in `PostDraft`)

```kotlin
data class PostDraft(
    val id: String,               // stable draft UUID
    val version: Int = 2,
    val images: List<DraftImage>, // ordered
    val location: DraftLocation?,
    val vibes: List<DraftVibe>,
    val vibeDisplayMode: VibeDisplayMode = Rotating,
    val imageVibeMap: Map<String, String> = emptyMap(),
    val updatedAt: Long,
)

data class DraftImage(
    val id: String,                     // stable image UUID
    val sortIndex: Int,
    val source: DraftImageSource,       // sealed: Gallery(uri) / Camera / RestoreFailed
    val originalPath: String?,          // local FS path to reversible orientation-normalized original (≤ 1600px)
    val previewPath: String?,           // rendered preview (down-scaled)
    val processingState: ProcessingState, // Ready / Processing / BlockedNeedsRemoval
    val edits: DraftImageEdits = DraftImageEdits.identity(),
    val addedAt: Long,
)

data class DraftImageEdits(
    val cropRect: Rect?,        // relative 0..1
    val rotationDegrees: Int,   // multiples of 90
    val straightenDegrees: Float, // -15..15
    val brightness: Float,      // -1..1
    val contrast: Float,        // -1..1
    val saturation: Float,      // -1..1
) {
    companion object { fun identity() = ... }
}
```

Legacy drafts (v1: bare `List<String>` URIs) migrate lazily to v2 on
first load: each URI becomes a `DraftImage` with `edits = identity()`
and `processingState = BlockedNeedsRemoval` if the file is missing.

### EXIF orientation

- On gallery import: read EXIF orientation via `ExifInterface`,
  bake it into the original bitmap, save orientation-normalized JPEG
  as `originalPath`.
- On camera capture: same normalization before showing preview.
- All previews and JPEG uploads read from `originalPath` — never the
  raw picker URI.

### Video prevention

- Photo picker `PickVisualMedia(ImageOnly)` — never `VisualMedia`.
- Camera intent: `MediaStore.ACTION_IMAGE_CAPTURE` only.
- Returned URI: verify MIME with `contentResolver.getType(uri)` starts
  with `image/`. Reject `video/*`, `application/octet-stream`, etc.
- `ImageIO`-style decode check: attempt `BitmapFactory.decodeStream`
  with `inJustDecodeBounds = true` — if it fails or width/height is
  0, reject.
- Uploader (in `SupabaseSpotPublishRepository`) whitelist:
  `Content-Type: image/jpeg` only.

### Transactional writes

- Drafts save as a **file set** (originals + previews + JSON). A
  partial write must not corrupt the prior valid draft.
- Implement via write-to-temp + atomic rename, or delegate to Room
  (see task 10) which gives transactions for free.

### Failed-import recovery

- If the picker/asset provider throws (limited-library permission,
  denied access), show a user-visible message with a **Try again**
  action. Do **not** log-and-silently-fail.
- Copy: `Couldn't import that photo. Try again.`

## iOS reference

- PR 71 (see `../spot-ios-app/`):
  - `Services/Spots/PostPhotoProcessor.swift` — EXIF + downscale.
  - `ViewModels/PostFlowViewModel.swift` — draft v2 lifecycle.
  - `Views/PostFlow/PhotoSelectionView.swift` — picker + camera.

## Android target (files to touch)

Create / edit:
- `data/post/PostDraft.kt` — v2 shape (see contract).
- `data/post/DraftImageEdits.kt` — new.
- `data/post/DraftImageSource.kt` — sealed class.
- `data/post/ImageProcessor.kt` — EXIF orientation baked in on import
  + camera capture.
- `data/post/FilePostDraftRepository.kt` — schema v2 read/write with
  atomic renames (or ship task 10 alongside for Room).
- `data/post/DraftMigrationV1toV2.kt` — legacy migration.
- `feature/post/PostViewModel.kt` — track v2 image list; per-image edits
  stored inline.
- `feature/post/PhotoEditor.kt` — apply `DraftImageEdits` to preview.
- `data/post/SupabaseSpotPublishRepository.kt` — upload the
  `originalPath` rendered file, MIME-check.
- `app/src/test/.../data/post/PostDraftMigrationTest.kt`,
  `ImageProcessorExifTest.kt`,
  `PostViewModelTest.kt` (new cases).

## Acceptance criteria

- [ ] Gallery import: EXIF orientation is baked in; preview and upload
      look identical to the source.
- [ ] Camera capture: same normalization; preview matches upload.
- [ ] Video URIs, non-image MIMEs, and zero-dimension files are
      rejected with the recovery message.
- [ ] Legacy v1 drafts load; missing files show `BlockedNeedsRemoval`.
- [ ] Reordering images across a session preserves stable image IDs
      (verify by dumping the draft JSON before and after).
- [ ] Interrupted save doesn't corrupt the draft.
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug` green.

## Test plan

- Manual: import a portrait photo shot in landscape → preview and
  upload are correctly oriented.
- Manual: try to pick a video → picker doesn't offer video items;
  side-loading a video URI is rejected.
- Manual: interrupt draft save (airplane mode + kill app) → reopen —
  either the prior draft is intact or the new one is atomic.

## Out of scope

- Cloud sync of drafts.
- HEIC support beyond conversion at import (JPEG-only upload stays).

## Follow-ups

- Ship task 10 (Room drafts) after this — the atomic-file work becomes
  simpler with Room transactions.
