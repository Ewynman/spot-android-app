# 08 — Post Flow (Create a Spot)

Tab index 2, test tag `navigation.postTab`. A **3-step composer** → background publish with image moderation. Supabase-only pipeline. Drafts are stored locally.

## Entry gating

| State | UI |
|-------|-----|
| Verifying email | Spinner |
| Email not verified | "Verify your email to post" + button opening the confirm-email flow |
| Verified | 3-step composer |

Posting requires an authenticated, email-verified user. RLS also denies publish for unauthenticated callers.

## Steps

`totalSteps = 3`. Each step gates the "next" action:

| Step | Screen | Proceed condition |
|------|--------|-------------------|
| 1 | Photo selection | at least one image selected |
| 2 | Location selection | a location is chosen |
| 3 | Vibe selection | at least one vibe selected |

Draft step enum (local): `photos`, `location`, `vibes`.

## Step 1 — Photos

- **Take Photo** → Camera pre-prompt → camera capture.
- **Choose from Gallery** → Photo pre-prompt → system photo picker (Android Photo Picker on 13+).
- **Max images**: free **1**, Pro **5** (`maxFreePostImages=1`, `maxProPostImages=5`).
  - Free user selecting multiple → keep only the first + show "Multiple images are available with Pro."
  - Pro user over 5 → "You can add up to 5 images per post."
- **Image processing**: downscale to **max 1600px** longest edge, JPEG encode (quality ~0.8) before upload. This keeps sizes/moderation consistent with iOS.
- **Drafts** button opens the drafts sheet.

## Step 2 — Location

- Confirm or select a place. iOS uses a canonical places JSON + search (`LocationSelectionView`). On Android use a place search (your maps provider's autocomplete or a curated dataset) and capture: latitude, longitude, a display `location_name`, optional address, and an `isCustomName` flag when the user types a custom name.
- The publish RPC only needs `latitude`, `longitude`, `location_name`.

## Step 3 — Vibes

- Choose from the **18 default vibe tags** (see [02-design-system.md](02-design-system.md)). Pro users can add **custom tags**.
- **Max vibes**: free **1**, Pro **5** (`maxFreePostVibes=1`, `maxProPostVibes=5`).
  - Free over 1 → "Multiple vibes are available with Pro."
  - Pro over 5 → "You can select up to 5 vibes."
- Custom tag validation: **2–30 chars**; blocked list rejects disallowed tags. Non-Pro attempting a custom tag → paywall.
- Vibe tags resolve to `vibe_tag_id`s: look up by `name_lower` in `vibe_tags`; create the row if it's a new Pro custom tag.

## Posting rules acknowledgement

First-time posters see a **posting rules** sheet; acceptance is stored (mirror `hasAcceptedPostingRules`). This is part of the UGC safety story.

## Publish pipeline (`SpotPublishCoordinator`)

Publishing is a **background, queued** operation so the user can keep using the app:

1. Build a publish draft (images, vibe ids, lat/lng, location name).
2. For each image: **insert a `media_assets` row** (`kind='spot_image'`, `status='pending'`, `pending_bucket='pending_images'`, `pending_path='{userId}/{assetId}.jpg'`, `mime_type='image/jpeg'`, byte size, width/height), then **upload the JPEG bytes** to `pending_images/{userId}/{assetId}.jpg`.
3. Call the **`moderate-image` edge function** per asset with `{ "mediaAssetId": "<uuid>" }` (JWT attached). Wait for `approved: true`.
4. When all assets are approved → call **`publish_spot_with_approved_media_assets_v1(p_vibe_tag_ids, p_latitude, p_longitude, p_location_name, p_media_asset_ids)`** which creates the `spots` row, `spot_images`, `spot_vibe_tags`, and returns the new spot id.
5. **Timeout: 90 seconds** for the whole publish.
6. On enqueue, switch to the **Home tab** and show an uploading **progress banner**.
7. On success: emit the "spot posted" signal (feed inserts it + toast), delete the autosaved draft.
8. On failure: show an error toast and a "post failed" signal; preserve the draft for retry.

### Failure states

| Condition | UX |
|-----------|-----|
| Not authenticated | Block publish, route to auth |
| Upload fails | Error; retry; draft preserved |
| Moderation rejects (`image_policy_rejected`) | Safe, non-graphic message; do not publish |
| Moderation unavailable (`moderation_unavailable`) | Retry later; do not publish |
| Publish RPC fails | Error; preserve draft |
| Network unavailable | Retry / offline messaging |

## Drafts (local only)

- Stored on-device (iOS: `Application Support/PostDrafts/`, JSON index). On Android use app-private files or Room.
- **Autosave** under a fixed id (`"autosave"`); manual **Save draft** creates a named draft and dismisses the composer.
- Draft summary fields: id, status (`autosaved`|`saved`), preview image filename, place name, vibe tags, updatedAt, step.
- Full draft: id, step (int), status, vibeTags, latitude?, longitude?, placeName?, address?, isCustomName, imageFileNames, updatedAt.
- Drafts are **not** synced to the server.

## Edit spot (Pro)

- Owner + Pro can edit a posted spot's **vibes and location** via `update_spot_metadata_v1(p_spot_id, p_vibe_tag_ids, p_latitude, p_longitude, p_location_name)`. Non-Pro → paywall. (Editing photos is not part of this RPC.)

## Delete spot

- Owner deletes their spot (from feed overflow or profile). Optimistic removal with rollback on failure; deletes the `spots` row (cascades handle images/junctions per FK config).

## States summary

- **Loading**: email verification check, image processing, upload/publish banner.
- **Empty**: no images/location/vibes yet → next disabled.
- **Error**: per failure table above.
- **Unauth/unverified**: blocked with routing.

## Constants

- Images: free 1 / Pro 5. Vibes: free 1 / Pro 5.
- Max image dimension: 1600px.
- Publish timeout: 90s.
- Vibe length: 2–30 chars.
