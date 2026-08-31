# AGENTS.md — how to work on this repo

You're an AI coding agent working on the **Spot Android** port. This file
tells you how to build things **the right way**: what to read first, what
tools you have, the patterns to follow, and what "done" means.

> **iOS is the reference.** When a spec is ambiguous, the iOS Swift source
> at `../spot-ios-app/` and the recent iOS PRs on
> [Ewynman/spot-ios-app](https://github.com/Ewynman/spot-ios-app) win.

---

## 1. Read order (mandatory)

Before you touch code, read these **in order**:

1. **[`.cursor/rules/project.mdc`](.cursor/rules/project.mdc)** — the
   authoritative rulebook. `alwaysApply: true`. If any doc contradicts it,
   it wins.
2. **[`docs/parity/constraints.md`](docs/parity/constraints.md)** — the
   golden rules distilled and expanded.
3. **[`docs/parity/limits-constants.md`](docs/parity/limits-constants.md)**
   — every numeric constant (feed 24, map 250, OTP 6/30 s, 1600 px, etc.).
4. **[`docs/parity/data-contracts.md`](docs/parity/data-contracts.md)** —
   RPCs, tables, storage buckets, edge functions.
5. **[`docs/parity/design-tokens.md`](docs/parity/design-tokens.md)** —
   colors, typography, spacing, copy.
6. **Your specific task file** in
   [`docs/parity/tasks/`](docs/parity/tasks/). Every task inlines the
   contract you need — you don't need the iOS repo checked out.

If you skipped any of the above, stop and read them.

> **Note on `PRD/`:** the old `PRD/` folder is deprecated and replaced by
> [`docs/parity/`](docs/parity/). Ignore any reference to `PRD/NN-*.md` —
> the same information lives under `docs/parity/`.

## 2. Skills you should use

These skills are installed and available. **Read the SKILL.md first** if
the task overlaps with what they cover.

| Skill | When to use |
|-------|-------------|
| `supabase/agent` | Any task touching Supabase — schema reads, RPC verification, RLS reasoning, migrations discussion. |
| `supabase-postgres-best-practices` | When you propose an RPC, index, or query change. |
| `firebase-basics`, `firebase-auth-basics` | When wiring `google-services.json`, App Check, FCM, or any Firebase SDK code. |
| `firebase-crashlytics` | If you touch crash reporting or add breadcrumbs. |
| `firebase-remote-config-basics` | Only if you're adding a runtime feature flag (mirror iOS `MapMarkerFeatureFlags` etc.). |
| `create-rule` | Only if you're **explicitly asked** to add a new Cursor rule. Do not add new rules opportunistically. |

**Rule:** Don't just "mention" a skill — if it's relevant, read the file and
follow it as your first action. Never claim to use a skill without reading it.

## 3. MCP tools you can use (verify contracts, don't guess)

These MCP namespaces are available in Cursor. Use them to **verify** what
you're about to ship — never assume a schema or key.

| Namespace | What to use it for |
|-----------|--------------------|
| `plugin-supabase-supabase` | `list_tables`, `get_advisors`, `execute_sql`, `list_edge_functions`, `apply_migration`. Use before writing a new DTO or RPC caller to confirm columns exist and types match. |
| `plugin-firebase-firebase` | `firebase_get_sdk_config`, `firebase_get_project`, `firebase_list_apps`. Use when replacing the placeholder `google-services.json`. |

**When in doubt, call an MCP tool and get the real answer.** "Guessing at
a column name" is a rejected-PR outcome.

Discover schemas with `GetDynamicTools({namespace: "plugin-supabase-supabase"})`.

## 4. Golden rules — the 10-second recap

1. **Same Supabase project as iOS.** No schema forks. Anon key only. RLS is authoritative.
2. **Every image passes `moderate-image`.** No client bypass.
3. **Safety in v1:** terms gate, moderation, report, block, content filtering.
4. **Preserve limits exactly** (see `limits-constants.md`).
5. **No Supabase calls from composables or ViewModels.** Only through a repository interface.
6. **Every repository has a fake** in `app/src/test/`.
7. **No inline hex.** Use `SpotColors.*` or `MaterialTheme.colorScheme.*`.
8. **No PII in logs.** Use `SpotLogger` per-area categories.
9. **Optimistic mutations** for like / bookmark / delete; roll back on failure.
10. **Test tags mirror iOS `accessibilityIdentifier` strings** (see `screen-map.md`).

## 5. Package layout — where things go

```
app/src/main/java/com/spot/android/
├── core/
│   ├── design/      ← theme, colors, dimensions, shared components (SpotCard, VibeChip, ...)
│   ├── supabase/    ← client, session bridge
│   ├── media/       ← Coil fetcher, signed URL cache, ImageProcessor
│   ├── analytics/   ← Firebase Analytics wrapper
│   ├── logging/     ← SpotLogger, LogPreferences
│   └── util/        ← Constants, LocationFormat, pure helpers
├── data/
│   ├── auth/        ← AuthRepository + impls
│   ├── billing/     ← Play Billing
│   ├── collections/ ← Pro collections
│   ├── content/     ← Cross-feature buses (LocalContentRemovalBus, ...)
│   ├── deeplink/    ← DeepLinkRouter, coordinator, SpotDetailRepository
│   ├── dto/         ← snake_case Postgres row DTOs
│   ├── feed/        ← FeedRepository, EngagementRepository, FeedEventService
│   ├── location/    ← FusedLocation, Places, viewer location
│   ├── map/         ← MapRepository, layout, viewport
│   ├── mapper/      ← DTO → domain model assembly
│   ├── model/       ← Spot, User, VibeTag, enums
│   ├── notifications/ ← SpotNotificationService, FCM (task 03)
│   ├── onboarding/  ← DataStore prefs
│   ├── permissions/ ← Runtime permission repo
│   ├── post/        ← Draft store, ImageProcessor, publish repo
│   ├── profile/     ← Profile + follow repos
│   ├── safety/      ← Report + block
│   ├── search/      ← Search repo, history store, grid loader
│   ├── settings/    ← Settings repo
│   ├── terms/       ← Terms acceptance
│   └── validator/   ← Email, username, vibe validators
├── di/              ← Hilt modules (one per data/* package)
├── feature/         ← One package per screen area
│   └── <area>/      ← Screen composables, sub-components, ViewModel, UI state
├── navigation/      ← Nav host, routes, bottom bar, overlay host, cross-tab buses
├── MainActivity.kt
└── SpotApplication.kt
```

**Do not invent a new top-level package.** If it doesn't fit, ask.

## 6. The Right Way — patterns to copy

Every snippet below is a working template. Copy it, adapt names, don't
re-derive the pattern.

### 6.1 A new repository (interface + real impl + fake)

**Interface** in `data/<area>/`:

```kotlin
package com.spot.android.data.<area>

interface WidgetRepository {
    suspend fun getWidget(id: String): Result<Widget>
    fun observeWidgets(): Flow<List<Widget>>
}
```

**Real impl** in `data/<area>/`:

```kotlin
package com.spot.android.data.<area>

import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.data.dto.WidgetRowDto
import com.spot.android.data.mapper.toDomain
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseWidgetRepository @Inject constructor(
    private val supabase: SupabaseClientProvider,
) : WidgetRepository {

    override suspend fun getWidget(id: String): Result<Widget> = runCatching {
        supabase.client.postgrest
            .from("widgets")
            .select { filter { eq("id", id) } }
            .decodeSingle<WidgetRowDto>()
            .toDomain()
    }

    override fun observeWidgets(): Flow<List<Widget>> = TODO()
}
```

**Hilt binding** in `di/<Area>Module.kt`:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetModule {
    @Binds
    @Singleton
    abstract fun bindWidgetRepository(
        impl: SupabaseWidgetRepository,
    ): WidgetRepository
}
```

**Fake** in `app/src/test/java/com/spot/android/data/<area>/FakeWidgetRepository.kt`:

```kotlin
class FakeWidgetRepository : WidgetRepository {
    var getWidgetResult: Result<Widget> = Result.success(Widget.stub())
    private val widgetsFlow = MutableStateFlow<List<Widget>>(emptyList())

    override suspend fun getWidget(id: String) = getWidgetResult
    override fun observeWidgets() = widgetsFlow.asStateFlow()

    fun seedWidgets(items: List<Widget>) { widgetsFlow.value = items }
}
```

### 6.2 A new DTO (snake_case, matches Postgres)

```kotlin
package com.spot.android.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WidgetRowDto(
    val id: String,                       // uuid
    @SerialName("user_id") val userId: String,
    val name: String,
    @SerialName("created_at") val createdAt: String,   // ISO-8601 timestamptz
    @SerialName("is_featured") val isFeatured: Boolean = false,
)
```

Rules:
- **Do NOT** apply a global snake_case → camelCase converter. Use
  `@SerialName` per field.
- Nullable fields: use `String?` and default to `null`. Never `!!`.
- Map to a domain model in `data/mapper/`; do not expose DTOs above `data/`.

### 6.3 A new screen (Compose + ViewModel + UiState)

**UiState + ViewModel** in `feature/<area>/<Area>ViewModel.kt`:

```kotlin
data class WidgetUiState(
    val load: LoadState = LoadState.Loading,
    val items: List<Widget> = emptyList(),
    val errorMessage: String? = null,
) {
    enum class LoadState { Loading, Loaded, Empty, Error }
}

@HiltViewModel
class WidgetViewModel @Inject constructor(
    private val repo: WidgetRepository,
    private val logger: SpotLogger,
) : ViewModel() {

    private val _state = MutableStateFlow(WidgetUiState())
    val state: StateFlow<WidgetUiState> = _state.asStateFlow()

    private val _effects = Channel<WidgetEffect>(Channel.BUFFERED)
    val effects: Flow<WidgetEffect> = _effects.receiveAsFlow()

    init { refresh() }

    fun refresh() {
        _state.update { it.copy(load = LoadState.Loading) }
        viewModelScope.launch {
            repo.getWidget("...")
                .onSuccess { widget ->
                    _state.update {
                        it.copy(
                            load = if (widget.items.isEmpty()) LoadState.Empty else LoadState.Loaded,
                            items = widget.items,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { t ->
                    logger.error(LogCategory.Widget, "load_failed", t)
                    _state.update {
                        it.copy(load = LoadState.Error, errorMessage = "Couldn't load. Try again.")
                    }
                }
        }
    }
}

sealed interface WidgetEffect {
    data class Navigate(val route: String) : WidgetEffect
    data class Toast(val message: String) : WidgetEffect
}
```

**Composable** in `feature/<area>/<Area>Screen.kt`:

```kotlin
@Composable
fun WidgetScreen(
    onNavigate: (String) -> Unit,
    viewModel: WidgetViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is WidgetEffect.Navigate -> onNavigate(effect.route)
                is WidgetEffect.Toast    -> /* show snackbar */ Unit
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotColors.background)
            .testTag("widget.screenRoot"),
    ) {
        when (state.load) {
            LoadState.Loading -> SkeletonList()
            LoadState.Empty   -> EmptyFeedView(title = "No widgets", body = "…")
            LoadState.Error   -> ErrorBanner(message = state.errorMessage.orEmpty())
            LoadState.Loaded  -> WidgetList(items = state.items, onItemClick = viewModel::onItemClicked)
        }
    }
}
```

**Never call Supabase, never do business logic in the composable.**

### 6.4 A ViewModel unit test with fakes

```kotlin
class WidgetViewModelTest {

    private lateinit var repo: FakeWidgetRepository
    private lateinit var logger: FakeSpotLogger
    private lateinit var vm: WidgetViewModel
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeWidgetRepository()
        logger = FakeSpotLogger()
        vm = WidgetViewModel(repo, logger)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `emits Loaded when repository returns items`() = runTest {
        repo.getWidgetResult = Result.success(Widget.stubList(3))
        vm.refresh()
        advanceUntilIdle()
        assertEquals(LoadState.Loaded, vm.state.value.load)
        assertEquals(3, vm.state.value.items.size)
    }

    @Test
    fun `emits Empty when repository returns empty list`() = runTest {
        repo.getWidgetResult = Result.success(Widget.stubList(0))
        vm.refresh()
        advanceUntilIdle()
        assertEquals(LoadState.Empty, vm.state.value.load)
    }

    @Test
    fun `emits Error and logs on failure`() = runTest {
        repo.getWidgetResult = Result.failure(IOException("boom"))
        vm.refresh()
        advanceUntilIdle()
        assertEquals(LoadState.Error, vm.state.value.load)
        assertNotNull(vm.state.value.errorMessage)
        assertTrue(logger.errors.any { it.tag == LogCategory.Widget })
    }
}
```

### 6.5 Optimistic mutation with rollback

```kotlin
fun toggleLike(spot: Spot) {
    val previous = _state.value
    _state.update { it.optimisticLike(spot.id) }   // pure state helper
    viewModelScope.launch {
        engagementRepo.setLiked(spot.id, liked = !previous.isLikedById(spot.id))
            .onFailure { t ->
                logger.error(LogCategory.Feed, "toggle_like_failed", t)
                _state.value = previous                 // rollback
                _effects.trySend(FeedEffect.Toast("Couldn't like. Try again."))
            }
    }
}
```

### 6.6 An auth-gated action

```kotlin
fun onLikeClicked(spot: Spot) {
    val user = session.current
    if (user == null) { _effects.trySend(Effect.NavigateToAuth); return }
    if (!user.emailVerified) { _effects.trySend(Effect.Toast(EMAIL_VERIFY_COPY)); return }
    toggleLike(spot)
}
```

### 6.7 Reading a signed spot image URL

```kotlin
// In a repository or hydrator — never in a composable.
val signedUrl = imageUrlSigner.signedUrl(
    path = dto.primary_storage_path,
    bucket = dto.source_bucket ?: DEFAULT_STORAGE_BUCKET,
)
```

`ImageUrlSigner` caches for ~7 days and re-signs at the 6 h threshold.
Never call `createSignedUrl` directly.

## 7. Design system — no inline hex

Use the theme:

```kotlin
Text(
    text = "SPOT",
    color = SpotColors.primary,                        // ✅
    style = MaterialTheme.typography.displayLarge,
)

// ❌ Never:
Text(text = "SPOT", color = Color(0xFF1D2C24))
```

Spacing / radius:

```kotlin
Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
    Box(modifier = Modifier.clip(RoundedCornerShape(Radius.medium)))
}
```

Full token list: [`docs/parity/design-tokens.md`](docs/parity/design-tokens.md).

## 8. Test tags — mirror iOS strings

```kotlin
Modifier.testTag("home.spotCard.like")     // ✅ matches iOS accessibilityIdentifier
Modifier.testTag("like_button")            // ❌ don't invent your own
```

Full vocabulary: [`docs/parity/screen-map.md`](docs/parity/screen-map.md).

## 9. Common pitfalls — the "not this" checklist

Do **not**:

- ❌ Call `supabase.postgrest.from(...)` inside a `@Composable` or ViewModel.
- ❌ Embed a service-role key, or any credentials, in source or `local.properties.template`.
- ❌ Inline hex colors (`Color(0xFF...)`), inline sizes (`8.dp`) when a token exists, or a magic number where `Constants.kt` has one.
- ❌ Skip the fake when you add a repository. VMs won't be testable.
- ❌ Emit feed events directly. Use `FeedEventService.record(...)` which coalesces.
- ❌ Log a token, an email, a UUID, or raw image bytes.
- ❌ Add a `try { } catch (e: Exception) { }` that swallows silently. Log with a category, surface UI error state, or rethrow.
- ❌ Copy iOS CI/CD, Xcode Cloud, xctestplans, or StoreKit configs into this repo.
- ❌ Restore or reference the deprecated `PRD/` folder.
- ❌ Add dark mode. v1 is light-only.
- ❌ Introduce a new Cursor rule unless the user explicitly asked. `create-rule` skill exists for this.
- ❌ Modify `.cursor/rules/project.mdc` without explicit approval.

Do:

- ✅ Handle all four states in every feature (loading / empty / error / unauthenticated).
- ✅ Use `Result<T>` for repository return types; map to UI state in the VM.
- ✅ Use `runCatching { ... }` at the Supabase boundary; never `try { ... } catch (Throwable)`.
- ✅ Debounce user input (300 ms) and `flatMapLatest` for search-like flows.
- ✅ Cancel in-flight jobs on new input.
- ✅ Prefer `StateFlow` for state and `Channel`/`SharedFlow` for one-shot effects.
- ✅ Use `@HiltViewModel` with constructor injection. No `@AndroidEntryPoint` fields on ViewModels.
- ✅ Scope `ViewModelScope` — never launch on `GlobalScope`.

## 10. Definition of done — every PR must pass all of these

- ✅ Task file's acceptance criteria all checked.
- ✅ Loading / empty / error / unauthenticated states implemented.
- ✅ ViewModel unit tests using fakes; all pass.
- ✅ `./gradlew assembleDebug` — clean.
- ✅ `./gradlew lintDebug` — clean (no new warnings; existing are OK).
- ✅ `./gradlew testDebugUnitTest` — green.
- ✅ Instrumented tests updated if UI surface changed.
- ✅ No hardcoded secrets; anon key only.
- ✅ No new PII in logs.
- ✅ No inline hex colors, no direct Supabase in composables/VMs, no new top-level packages.
- ✅ Test tags match iOS `accessibilityIdentifier` strings where practical.
- ✅ Task file's row moved from Open → Shipped in [`task-queue.md`](docs/parity/task-queue.md).

## 11. PR format

Title:

```
parity/<task-id>: <one-line summary>
```

Example: `parity/12-map-photo-pins: circular photo-preview map markers with clustering`.

Body:

```markdown
## Scope
<what shipped, 3–5 bullets>

## Acceptance criteria
<copy the checklist from the task file, check the boxes>

## Screenshots / recording
<UI change → screenshots; if the flow is non-trivial, a short screen recording>

## Verification
- ./gradlew assembleDebug ✅
- ./gradlew lintDebug ✅
- ./gradlew testDebugUnitTest ✅ (N tests)
- Manual: <one-line summary of what you tested>

## iOS PR reference
<link if this derives from an iOS PR>

## Follow-ups
<anything you noticed but did NOT fix — with a short rationale>

## Decisions
<any choice you made where the task file was silent — copy string, animation, etc.>
```

## 12. When to stop and escalate

Stop and open a **draft** PR with a description if any of these happen:

- The task requires a Supabase migration you can't verify (`plugin-supabase-supabase.list_migrations` shows the migration is missing on staging).
- An RPC signature doesn't match what the task file says. Verify with `execute_sql` first; if still off, escalate.
- The acceptance criteria conflict with `constraints.md`. Constraints win.
- Compile or tests fail after 3 targeted fix attempts.
- The task is a design-token decision the docs don't cover (new color, new copy string).
- The task touches auth, moderation, or safety in a way that could reduce guardrails. **These do not get "small quick fixes."**

Never:
- **Delete** or **restore** `PRD/`.
- **Modify** `.cursor/rules/project.mdc` without explicit user approval.
- Ship a client-side workaround for a missing server contract. Fix the server.

## 13. Quick reference — where things live

| Need to… | Look at |
|----------|---------|
| Read the rules | [`.cursor/rules/project.mdc`](.cursor/rules/project.mdc) |
| Pick a task | [`docs/parity/task-queue.md`](docs/parity/task-queue.md) |
| Understand a task | `docs/parity/tasks/<id>-*.md` |
| Look up an RPC / table | [`docs/parity/data-contracts.md`](docs/parity/data-contracts.md) |
| Look up a limit | [`docs/parity/limits-constants.md`](docs/parity/limits-constants.md) |
| Look up a color / spacing / copy | [`docs/parity/design-tokens.md`](docs/parity/design-tokens.md) |
| Find the iOS equivalent screen / file | [`docs/parity/screen-map.md`](docs/parity/screen-map.md) |
| Launch a Cloud Agent on a task | [`docs/parity/agent-brief.md`](docs/parity/agent-brief.md) |
| Verify a Supabase contract | MCP: `plugin-supabase-supabase` |
| Verify Firebase config | MCP: `plugin-firebase-firebase` |
| Add drafts / prefs schema | [`docs/parity/tasks/10-post-drafts-migrate-to-room.md`](docs/parity/tasks/10-post-drafts-migrate-to-room.md) |
| Extend structured logging | [`docs/parity/tasks/30-logging-profiles-parity.md`](docs/parity/tasks/30-logging-profiles-parity.md) |

---

**One more time, because it matters:** iOS is the reference for
behavior. `constraints.md` is authoritative for what you can and can't
do. Real Supabase is authoritative for the schema. Real Firebase is
authoritative for config. **When in doubt, verify. Don't guess.**
