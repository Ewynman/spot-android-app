# Task 17 — Optimistic feed author + bucket-aware image signing

**Size:** Small (1–3 h; may already work) • **Priority:** P2
**Status:** Open
**iOS reference PR:** [Ewynman/spot-ios-app #75](https://github.com/Ewynman/spot-ios-app/pull/75)

## Goal

Verify + fix: (1) optimistic home-feed inserts carry the current user's
`username` and `profile_image_url`, and (2) primary spot images are
signed **per bucket** using `primary_storage_bucket` from
`get_home_feed_v1` / `get_map_spots_v1`.

## Contract

### Optimistic author

After publish success, `HomeFeedViewModel` synthesizes a `Spot` and
pins it at the top of the feed. This spot must include:

- `author.username` from `UserSessionHolder.current.username`.
- `author.profileImageUrl` from `UserSessionHolder.current.profileImageUrl`.
- The freshly-published image URL (client already has the approved
  media asset URL from `moderate-image` response).
- Correct `storage_bucket` on the image (the bucket the publish RPC
  wrote to — typically `spots`).

If those fields are missing, the top card shows a blank author row
until the next refresh.

### Bucket-aware signing

Feed and map RPCs return a `primary_storage_bucket` per row. When
requesting signed URLs from `ImageUrlSigner`, pass this bucket rather
than defaulting to the historical `"spots"` bucket.

- `HomeFeedRowDto` has `source_bucket` (verify field name in the
  actual RPC response — may be `primary_storage_bucket`; align with
  server).
- `MapSpotRowDto` — same.
- `ImageUrlSigner.signedUrl(path, bucket)` — pass the bucket through.

Moderated assets that live in a distinct bucket (e.g.
`approved_spot_images`) currently render as gray skeletons because the
signer defaults to `spots`.

## iOS reference

- PR 75 files:
  - `Services/Feed/FeedAPI.swift` — `primary_storage_bucket` decode.
  - `Views/Components/FeedContentView.swift` — optimistic insert.
  - `Services/Supabase/SpotSupabaseRepository.swift` —
    `resolveStoredImageURLs(bucket:)`.

## Android target (files to touch)

Verify:
- `data/dto/HomeFeedRowDto.kt` — has `source_bucket` (or aligned name).
- `data/dto/MapSpotRowDto.kt` — same.
- `data/mapper/SpotMapper.kt` — passes bucket through to model.
- `core/media/ImageUrlSigner.kt` — accepts a `bucket` param.

Fix (if the audit finds gaps):
- `feature/home/HomeFeedViewModel.kt` — after publish, hydrate the
  synthetic `Spot` with `author.username`, `author.profileImageUrl`,
  and the correct `storage_bucket` on the image.
- `core/media/SpotImageFetcher.kt` — request signed URL with the
  right bucket.

Tests:
- `HomeFeedViewModelTest` — new case: post → optimistic insert has
  full author fields.
- `ImageUrlSignerTest` — sign a URL for a non-default bucket.

## Acceptance criteria

- [ ] After publish, the top card shows the poster's username and
      avatar immediately.
- [ ] Pull-to-refresh loads moderated spots (in `approved_spot_images`
      or any non-default bucket) without gray skeletons.
- [ ] Map pins load primary images for the same rows.
- [ ] Signed URL cache keys by `(bucket, path)` so cross-bucket
      collisions don't happen.
- [ ] `./gradlew testDebugUnitTest lintDebug assembleDebug` green.

## Test plan

- Manual: publish a new spot → top card shows author.
- Manual: pull-to-refresh → moderated spots load.
- Manual: check map: moderated spots' pins load photo (task 12) or at
  least the correct primary image URL.

## Out of scope

- Any RPC signature change (bucket already returned).
- Rewriting Coil's cache.

## Follow-ups

- Add a lint check that flags direct hardcoded `"spots"` bucket
  strings.
