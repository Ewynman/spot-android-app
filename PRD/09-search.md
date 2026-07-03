# 09 — Search

Tab index 3, test tag `navigation.searchTab`. Three segments — **Users**, **Locations**, **Vibes** — with debounced query, per-segment history, and tappable result grids.

## Segments

`Segment`: `users` ("Users"), `locations` ("Locations"), `vibes` ("Vibes"). A segmented control switches between them.

## Query behavior

- **Debounce: 0.3s** before firing a search.
- **Empty query**:
  - Users/Locations: show **search history** for that segment (max **20** items, most recent first).
  - Vibes: show **all vibe tags** (18 defaults + any custom from the catalog).
- **Active query** per segment:

| Segment | Query | Empty copy |
|---------|-------|------------|
| Users | prefix search over `users_public` (username) | "No users found" |
| Locations | location name suggestions (distinct `spots.location_name` ILIKE) | "No locations found" |
| Vibes | vibe tag suggestions (from `vibe_tags`) | "No vibes found" |

## Search history

- Item shape: id (uuid), type (`user`|`location`|`vibe`), query, displayText, timestamp.
- Stored locally (DataStore/Room), max 20 per segment, key analogous to `search_history_v1`.
- Tapping a history item re-runs that search / opens that grid.

## Result actions → grids

- **User row** → navigate to that user's **profile**.
- **Location result** → open a **location grid**: paginated spots whose `location_name` matches.
- **Vibe result** → open a **vibe grid**: paginated spots with that vibe (via `list_spot_ids_for_vibe_search_v1`).
- **Multi-vibe** (Pro) → grid across several vibes.

## Grids

- Grid shows spots as a 2-column (or similar) grid of covers; tapping expands an inline `SpotCard` with a back-to-grid affordance.
- **Pagination**: target **24** spots per page load; up to **5** fetch attempts per load to fill a page (because id→spot hydration can drop private/blocked/hidden rows). Cursor is an offset; `hasMore` when the RPC returns a next cursor.
- Backing RPCs:
  - Vibe grid: `list_spot_ids_for_vibe_search_v1(p_vibe_tag_ids, p_limit, p_offset)` → hydrate spot ids.
  - Location+vibe grid (Pro): `list_spot_ids_for_location_and_vibe_search_v1(p_location_pattern, p_vibe_tag_ids, p_limit, p_offset)`. `p_location_pattern` is ILIKE-escaped.
  - Plain location grid: query `spots` by `location_name` ILIKE (with RLS filtering).

## Pro advanced filters

- On a **location grid** (not a vibe grid), Pro users see a **Filter** button to add multi-vibe filters, switching the query to `list_spot_ids_for_location_and_vibe_search_v1`.
- Filter sheet lists all vibe tags (defaults + custom). Non-Pro: filter button hidden.
- Visibility rule: show filter when `isPro && gridTitle != null && !gridIsVibe`.

## States

- **Loading**: spinner while searching / loading grid page.
- **Empty**: per-segment "No … found" copy; empty grid → context-appropriate empty message.
- **Error**: network error toast; keep previous results where possible.
- **History empty**: nothing shown (or a subtle hint).

## Android implementation notes

- Debounce with `snapshotFlow`/`debounce(300)` on the query text.
- Grids: `LazyVerticalGrid` with manual paging matching the 24-target / 5-attempt fill semantics so pages don't render half-empty after RLS filtering.
- Reselecting the Search tab can clear/reset the current query and return to history.
