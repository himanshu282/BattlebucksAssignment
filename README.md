# Real-Time Leaderboard — BattleBucks Assignment

A Jetpack Compose Android app that simulates a live gaming leaderboard with two independent modules: a **Score Generator Engine** and a **Leaderboard Consumer**. The UI updates in real time as random score events arrive, with a collapsing hero header and safe-area support.

## How to Run the Project

### Prerequisites

- Android Studio Ladybug (2024.2) or newer
- JDK 11+
- Android SDK 36 (compile SDK)
- A device or emulator running API 24+

### Run on device / emulator

1. Clone the repository and open it in Android Studio.
2. Let Gradle sync finish.
3. Select a run configuration for the `app` module.
4. Click **Run** (or `./gradlew :app:installDebug` from the project root).

### Run unit tests

```bash
./gradlew :app:testDebugUnitTest
```

### Build debug APK

```bash
./gradlew :app:assembleDebug
```

The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

---

## Module Responsibilities

### Module 1 — Score Generator Engine (`com.assignment.engine`)

Simulates a game backend that emits score events continuously.

| Component | Responsibility |
|---|---|
| `Player` | Player identity (`id`, `username`) |
| `ScoreEvent` | A single score increment for one player |
| `ScoreGenerator` | Contract: `scoreUpdates(): Flow<ScoreEvent>` |
| `RandomScoreGenerator` | Emits random events forever using a seeded `Random` |

**Behaviour**

- Random interval between **500ms** and **2000ms**
- Random player selected each tick
- Score increment between **1** and **20**
- Scores only increase (positive increments only)
- Deterministic per session via `seed`
- No Android dependencies — pure Kotlin + Coroutines

### Module 2 — Leaderboard Domain (`com.assignment.domain`)

Consumes score events and maintains ranked leaderboard state. **Does not generate scores.**

| Component | Responsibility |
|---|---|
| `LeaderboardEntry` | Ranked row: `rank`, `playerId`, `username`, `score` |
| `RankingCalculator` | Competition ranking (ties share rank; next rank skips) |
| `LeaderboardUseCase` | Accumulates scores, recalculates ranks, exposes `StateFlow` |

**Business rules**

- Sorted by score descending
- Same score → same rank (`100, 100 → 1, 1` then `90 → 3`)
- Publishes only when the leaderboard list actually changes

### Supporting Layers

| Package | Responsibility |
|---|---|
| `com.assignment.core` | `LeaderboardConfig`, `LeaderboardFactory` — wires engine + domain |
| `com.assignment.ui` | ViewModel, Compose screens, collapsing hero, list items |
| `com.himanshu.assignment` | `MainActivity`, Material theme |

---

## Architecture Overview

```
┌────────────────────────────────────────────────────────────────┐
│                         UI Layer                               │
│  LeaderboardScreen → LeaderboardViewModel → LeaderboardUiState │
│  (Compose only — no ranking or score logic)                    │
└───────────────────────────┬────────────────────────────────────┘
                            │ collectAsStateWithLifecycle
┌───────────────────────────▼─────────────────────────────────┐
│                      Domain Layer                           │
│  LeaderboardUseCase → RankingCalculator                     │
│  StateFlow<List<LeaderboardEntry>>                          │
└───────────────────────────┬─────────────────────────────────┘
                            │ Flow<ScoreEvent>
┌───────────────────────────▼─────────────────────────────────┐
│                      Engine Layer                           │
│  RandomScoreGenerator : ScoreGenerator                      │
│  (UI-agnostic, testable, reusable)                          │
└─────────────────────────────────────────────────────────────┘
```

**Data flow**

1. `RandomScoreGenerator` emits `ScoreEvent` on a cold `Flow`.
2. `LeaderboardUseCase` collects events on `Dispatchers.Default`, updates an internal score map, and recalculates via `RankingCalculator`.
3. `LeaderboardViewModel` maps domain state to `LeaderboardUiState` (entries + current user) and exposes it via `StateFlow`.
4. `LeaderboardScreen` renders a collapsing hero (logged-in user rank/score) and a `LazyColumn` of all players.

**MVVM + Clean Architecture principles**

- Strict separation: score generation, ranking logic, and UI rendering live in separate packages.
- ViewModel has no ranking or score-generation logic.
- Compose UI has no business logic.

**UI highlights**

- CoordinatorLayout-style collapsing hero via `NestedScrollConnection`
- Logged-in user shown in hero (expanded + collapsed bar) and in the list when ranked
- Safe area: `statusBarsPadding()` on hero, navigation bar `contentPadding` on list
- Stable `LazyColumn` keys, lightweight score-change animation

---

## Design Note — Performance & Lifecycle

### How we avoid blocking the UI thread

| Technique | Where |
|---|---|
| Score generation runs on `Dispatchers.Default` | `RandomScoreGenerator.flowOn(Dispatchers.Default)` |
| Flow collection and score accumulation off Main | `LeaderboardUseCase.start()` launches on `collectionDispatcher` (default: `Dispatchers.Default`) |
| Ranking recalculation on background thread | Happens inside the collection coroutine, never on Main |
| UI only observes immutable state | `collectAsStateWithLifecycle()` — Compose reads `StateFlow`, does not compute ranks |

No blocking I/O or heavy synchronous work runs on the Main thread. The UI thread only handles composition and rendering.

### How we avoid unnecessary recompositions

| Technique | Where |
|---|---|
| Immutable `LeaderboardUiState` | Single snapshot consumed by the screen |
| `StateFlow.update` with equality check | `LeaderboardUseCase` skips emission when the ranked list is unchanged |
| `SharingStarted.WhileSubscribed(5_000)` | ViewModel stops upstream work shortly after UI leaves composition |
| Stable `LazyColumn` keys | `key = { it.playerId }` preserves item identity across updates |
| `contentType` on list items | Helps Compose recycle row composables efficiently |
| `remember` for static brushes | Background gradient not recreated on every recomposition |
| Split `LaunchedEffect` per item | Rank-up and score-highlight animations are independent, avoiding blocked effects |
| Presentation mapping in ViewModel | `currentUserEntry` derived once in `LeaderboardUiState.from()`, not in Composables |

### How we avoid memory leaks

| Technique | Where |
|---|---|
| `viewModelScope` for collection | Coroutine cancelled automatically when ViewModel is cleared |
| `WhileSubscribed` on `stateIn` | Stops collecting when no active UI subscribers |
| Single guarded collector | `AtomicBoolean` in `start()` prevents duplicate Flow subscriptions |
| No static references to Activity/Context in domain/engine | Engine and domain are Android-free |
| `collectAsStateWithLifecycle` | Collection pauses when the screen is stopped, lifecycle-aware |
| `LaunchedEffect` keyed by `playerId` | Item-level effects are scoped to composable lifetime |

**Known gap:** Process death (low-memory kill) clears the ViewModel and all scores. There is no `SavedStateHandle` or persistent cache yet — see lifecycle section below.

---

### Behaviour on screen rotation

| Aspect | Current behaviour |
|---|---|
| ViewModel | Survives rotation by default (no `configChanges` override needed) |
| Score stream | Continues via `viewModelScope` — collection is not restarted |
| Leaderboard state | Preserved in `LeaderboardUseCase` inside the surviving ViewModel |
| Collapse scroll offset | **Resets** — `CollapsingHeroScrollState` is `remember`ed in Compose and is not saved |
| List scroll position | **Resets** — `LazyListState` is not saved |

**With more time:** persist collapse offset and list scroll in `rememberSaveable`, or hoist scroll state into the ViewModel / `SavedStateHandle`.

### Behaviour when the app goes to background

| Aspect | Current behaviour |
|---|---|
| UI collection | `collectAsStateWithLifecycle` stops emitting when the screen is not at least `STARTED` |
| Upstream Flow | `WhileSubscribed(5_000)` cancels the ViewModel → UseCase mapping subscription after ~5s with no active collectors |
| Score generator | Keeps running while `viewModelScope` is alive; pauses when ViewModel is eventually cleared (process death or app kill) |
| Battery / CPU | Acceptable for a demo with 8 players; a production app would pause the engine in `onStop` or move it to a foreground service / server |

**With more time:** tie `ScoreGenerator` lifecycle to `ProcessLifecycleOwner` or a repository that pauses collection when the app is backgrounded.

---

### How we would scale this

#### ~1K users

Current architecture holds with minor changes:

- **In-memory score map** (`HashMap<String, Int>`) — fine for 1K entries (~few KB).
- **Full recalculation** via `RankingCalculator` — O(n log n) sort on ~1K rows is sub-millisecond on device.
- **UI:** `LazyColumn` virtualizes rows; only visible items compose.
- **Changes:** throttle emissions (e.g. coalesce updates every 100ms), batch score events before recalculating once per batch.

#### ~100K users

Requires architectural shifts:

| Layer | Approach |
|---|---|
| **Engine** | Move score generation to a backend (WebSocket). Client consumes events, does not simulate. |
| **Domain** | Incremental ranking — update only affected ranks instead of full sort on every event. Or server-side ranking with client displaying a window. |
| **State** | Paginated / windowed leaderboard (top 100 + current user neighbourhood). Do not hold 100K `LeaderboardEntry` objects in memory. |
| **UI** | `LazyColumn` with pagination (`Paging 3`), diff util for partial updates, avoid full list replacement in `StateFlow`. |
| **Networking** | Backpressure-aware stream; client applies events on `Dispatchers.Default`, publishes throttled snapshots to UI. |

At 100K scale, ranking should not live on the client for the full dataset — the phone receives a **viewport** (e.g. ranks 1–50 + user's rank) from the server.

---

## Architecture & Decisions

### Why we split modules this way

```
engine  →  domain  →  ui  (+ core for wiring)
```

| Package | Why it exists |
|---|---|
| **`engine`** | Simulates an external game backend. Pure Kotlin, no Android. Could be reused in unit tests, JVM benchmarks, or swapped for a real network adapter without touching UI. |
| **`domain`** | Owns business rules and state. Consumes `ScoreGenerator` but never generates scores — matches the assignment's consumer constraint. |
| **`ui`** | Rendering and lifecycle only. Depends on domain models, not on `RandomScoreGenerator` directly. |
| **`core`** | Composition root for the app — creates engine + use case + config. Keeps ViewModel free of `new RandomScoreGenerator(...)` calls. |

This mirrors **Clean Architecture dependency direction**: UI → Domain → Engine (via interfaces). The assignment's two modules map directly to `engine` and `domain`; `ui` and `core` are the Android delivery layer.

### Where ranking logic lives and why

**`RankingCalculator` in `com.assignment.domain`** — and nowhere else.

| Layer | Has ranking logic? |
|---|---|
| `RankingCalculator` | ✅ Yes — competition ranking algorithm |
| `LeaderboardUseCase` | Calls `RankingCalculator`; maintains scores only |
| `LeaderboardViewModel` | ❌ No — maps `StateFlow` to `LeaderboardUiState` |
| Compose UI | ❌ No — displays `rank`, `username`, `score` as-is |

**Why domain, not ViewModel?**

- Ranking is a **business rule** (ties, rank skipping, sort order), not presentation logic.
- `RankingCalculator` is a pure function — trivially unit testable without Robolectric or Compose.
- ViewModel stays thin: subscribe, map, expose. If ranking rules change, only domain changes.

**Why not in the UseCase class body?**

- Single Responsibility: UseCase orchestrates flow collection and state; `RankingCalculator` owns the algorithm.
- Testability: five focused tests on `RankingCalculator` without mocking coroutines.

### Trade-offs we consciously made

| Decision | Rationale |
|---|---|
| **Single app module** instead of multi-module Gradle project | Faster setup for an assignment; packages still enforce layer boundaries. |
| **Cold `Flow` with single collector** | Keeps the engine simple and testable; `LeaderboardUseCase.start()` guards against double collection. |
| **`LeaderboardFactory` object** | Lightweight DI without Hilt/Koin for this scope. |
| **Player list passed to both engine and use case** | Engine needs players for random selection; use case needs them for ranking. Duplication is acceptable at this scale. |
| **Current user ID in config, not auth** | No real login; `player_8` (Himanshu) stands in for the device user. |
| **`StateFlow.update` equality check** | Avoids redundant emissions when recalculated list is unchanged; reduces Compose recompositions. |
| **Hero height driven by scroll offset, not `animateDpAsState`** | Finger-linked collapse feels more natural (CoordinatorLayout behaviour). |
| **Full list in `StateFlow`** | Simple and correct for 8 players; would switch to windowed/paginated state at scale. |
| **Rank-up animation in UI** | Fast to ship; domain would own `rankDelta` in a production app. |
| **No pause on background** | Demo prioritises continuous live updates over battery optimisation. |

---

## What I'd Improve With More Time

1. **Gradle multi-module split** — `engine` and `domain` as pure Kotlin modules with zero Android deps; stricter compile-time boundaries.
2. **Hilt / Koin DI** — replace `LeaderboardFactory` with injectable interfaces for production and test doubles.
3. **`SharedFlow` or channel-based engine** — if multiple consumers ever need the same event stream without restarting the cold flow.
4. **`SavedStateHandle`** — survive process death; restore scores and collapse state.
5. **Rank-change metadata in domain** — expose `rankDelta` from the use case instead of inferring it in `LeaderboardItem` composables.
6. **Compose `animateItem()`** — smoother list reordering when ranks change.
7. **Pagination / large leaderboards** — virtualize or window rankings for hundreds of players.
8. **Real auth integration** — resolve current user from session instead of a hardcoded `currentUserId`.

---

## Test Coverage

| Test class | What it verifies |
|---|---|
| `RankingCalculatorTest` | Empty list, single player, ties, rank skipping, alphabetical tie-break, descending order |
| `LeaderboardUseCaseTest` | Score accumulation, unknown player ignored, idempotent `start()` |
| `RandomScoreGeneratorTest` | Valid event range, session determinism, empty player guard |
| `LeaderboardViewModelTest` | `uiState` maps current user correctly |
| `LeaderboardUiStateTest` | Current user kept in list when ranked |

---

## Project Structure

```
com.assignment/
├── engine/          # Score generation (no Android)
├── domain/          # Ranking + use case
├── ui/
│   ├── leaderboard/ # Screen, ViewModel, UiState
│   └── components/    # Hero, list item, collapsing scroll
└── core/            # Config + factory wiring
```

---

## Code Review (Mid-Level Submission Review)

Review of this codebase as if submitted by a mid-level engineer. Eight comments with category and reasoning.

### 1. Must Fix — Score events are not validated in the domain layer

**Where:** `LeaderboardUseCase.applyScoreEvent()`

**Comment:** The use case blindly adds `event.scoreIncrement` without checking it is positive. The engine only emits `1–20`, but the consumer module should enforce the business rule *"scores only increase"* at the boundary. A buggy or malicious `ScoreGenerator` could pass negative values.

**Reasoning:** Domain layer must protect invariants regardless of upstream behaviour. Defence in depth is standard for consumer modules.

---

### 2. Must Fix — Score generator runs while app is backgrounded

**Where:** `LeaderboardViewModel.init` → `leaderboardUseCase.start(viewModelScope)`

**Comment:** The infinite `while (true)` flow keeps emitting on `Dispatchers.Default` even when the user is not looking at the leaderboard. `WhileSubscribed` stops the ViewModel mapping, but the UseCase collector remains active while the ViewModel lives.

**Reasoning:** Battery and CPU waste. Production apps should pause collection via `ProcessLifecycleOwner` or move streaming to a server-driven push model.

---

### 3. Must Fix — Hardcoded current user ID

**Where:** `LeaderboardFactory.CURRENT_USER_ID = "player_8"`

**Comment:** The logged-in user is hardcoded. Hero section and `LeaderboardUiState` depend on this constant instead of an auth/session source.

**Reasoning:** Blocks real multi-user deployment. Acceptable for a demo; must be replaced before production.

---

### 4. Improvement — Rank-change detection lives in Compose

**Where:** `LeaderboardItem` — `LaunchedEffect(entry.rank)` compares `previousRank`

**Comment:** UI infers rank improvements by remembering prior rank locally. Scrolled-off items reset state; fast rank changes can miss or duplicate the green arrow indicator.

**Reasoning:** Presentation side-effects (rank delta) belong in domain or ViewModel. UI should render `entry.rankDelta` from immutable state.

---

### 5. Improvement — Collapse and list scroll state are not saved

**Where:** `rememberCollapsingHeroScrollState()`, `rememberLazyListState()` in `LeaderboardScreen`

**Comment:** On rotation, hero collapse offset and list scroll position reset. ViewModel preserves scores, but UX state is lost.

**Reasoning:** Users expect scroll position to survive rotation. Use `rememberSaveable` or hoist into `SavedStateHandle`.

---

### 6. Improvement — Cold `Flow` contract is convention-only

**Where:** `RandomScoreGenerator.scoreUpdates()`

**Comment:** Each call returns a new cold flow. Correctness depends on `AtomicBoolean` in `start()` to prevent double collection. This is fragile if a second caller bypasses `start()`.

**Reasoning:** A `SharedFlow`/`channelFlow` owned by a single repository makes the single-collector contract structural, not cultural.

---

### 7. Tech Debt — Single Gradle module with package separation

**Where:** Project structure

**Comment:** `engine`, `domain`, and `ui` are packages, not modules. Nothing prevents `ui` from importing `RandomScoreGenerator` directly and bypassing the domain layer.

**Reasoning:** Assignment-appropriate trade-off. Multi-module Gradle would enforce dependency direction at compile time.

---

### 8. Tech Debt — Player list owned by both engine and use case

**Where:** `LeaderboardFactory` passes `players` to `RandomScoreGenerator` and `LeaderboardUseCase`

**Comment:** Two copies of the same list. If a player is added to one and not the other, scores and random selection diverge.

**Reasoning:** A single `PlayerRepository` or shared immutable `Players` value object would be the long-term fix.

---

## Planning & Ownership

### If we must ship in 7 days — what is non-negotiable vs what we cut

#### Non-negotiable (Days 1–5)

| Item | Why |
|---|---|
| `ScoreGenerator` + `RandomScoreGenerator` (Module 1) | Core assignment requirement |
| `LeaderboardUseCase` + `RankingCalculator` (Module 2) | Core assignment requirement |
| Competition ranking (ties + skip) | Explicit business rule |
| `StateFlow` exposure to UI | Reactive stream requirement |
| Basic `LazyColumn` leaderboard UI | Must demonstrate real-time updates |
| Unit tests for `RankingCalculator` | Correctness proof for ranking rules |
| README (run, architecture, trade-offs) | Submission requirement |

#### Cut or defer (Days 6–7 polish only if time remains)

| Cut / Defer | Why it can wait |
|---|---|
| Collapsing hero + `NestedScrollConnection` | Visual polish, not core logic |
| Score highlight / rank-up animations | UX enhancement |
| Safe area / edge-to-edge fine-tuning | Polish |
| Custom avatars / hero gradient | Design polish |
| `SavedStateHandle` / rotation scroll restore | UX improvement, not MVP |
| Multi-module Gradle split | Architecture ideal, not MVP |
| Hilt / Koin DI | Factory is sufficient for demo |
| Instrumented / screenshot tests | Manual QA acceptable in 7 days |
| Background pause for score generator | Document as known gap |
| Anti-cheat / server backend | Out of scope for 7-day client MVP |

#### 7-day timeline (lead view)

| Day | Focus |
|---|---|
| 1 | Engine module + tests |
| 2 | Domain module + ranking tests |
| 3 | ViewModel + basic Compose list |
| 4 | Wire factory, lifecycle, StateFlow |
| 5 | Unit tests, bug fixes, README architecture |
| 6 | UI polish (hero, current user bar) |
| 7 | Code review, final README, buffer |

---

### How I would divide work

#### Junior developer

- Compose list row (`LeaderboardItem`) — layout only, no logic
- Theme colours and typography
- `PlayerAvatar` / `ScoreBadge` components
- Safe area padding (`statusBarsPadding`, navigation bar insets)
- Write README "How to run" section
- Manual test checklist execution

**Why:** Visual, well-scoped tasks with clear designs. No coroutine or ranking logic.

#### Mid-level developer

- `LeaderboardUseCase` + `RankingCalculator`
- `LeaderboardViewModel` + `LeaderboardUiState`
- `LeaderboardScreen` + `collectAsStateWithLifecycle`
- Unit tests: `RankingCalculatorTest`, `LeaderboardUseCaseTest`
- `LeaderboardFactory` wiring
- README architecture + module responsibilities sections

**Why:** Owns the data flow and business rules end-to-end. Can work with minimal supervision.

#### Lead (me)

- `ScoreGenerator` interface design + `RandomScoreGenerator`
- Package / layer boundaries and code review
- Collapsing hero scroll architecture (`NestedScrollConnection`)
- Performance review (Main thread, recompositions, leaks)
- README design note, trade-offs, scaling, code review section
- 7-day scope decisions and unblocking juniors on integration

**Why:** System design, cross-cutting concerns, and risk decisions require senior ownership.

---

## Optional — Strong Lead Signals

### Anti-cheat ideas for live tournaments

| Threat | Mitigation |
|---|---|
| Client-side score manipulation | **Never trust the client.** Scores must be computed and signed server-side. Client only displays server-authoritative leaderboard snapshots. |
| Replay / forged events | Server issues signed `ScoreEvent` tokens (HMAC/JWT) with `playerId`, `matchId`, `increment`, `timestamp`, `nonce`. Client cannot invent events. |
| Speed hacking / inflated increments | Server validates increment against game rules per match type (max points per action). Rate-limit events per player per second. |
| Multi-account farming | Device fingerprint + account binding; anomaly detection on score velocity vs historical percentile. |
| Leaderboard sniping at deadline | Freeze window (e.g. last 60s of season locks submissions); server clock is source of truth, not device time. |
| Tie manipulation | Server-side ranking with deterministic tie-break (timestamp of achievement, not client order). |
| Spectator / bot flooding | Authenticate WebSocket connections; throttle subscription fan-out per tournament. |

**Architecture shift:** `ScoreGenerator` on device becomes a **network adapter** implementing `ScoreGenerator` by collecting from a authenticated server stream. `RankingCalculator` moves server-side for tournaments; client shows a viewport.

---

### Production readiness improvements

| Area | Action |
|---|---|
| **Persistence** | Cache last leaderboard snapshot in Room; restore on cold start |
| **Process death** | `SavedStateHandle` for scores + scroll state |
| **DI** | Hilt modules: `EngineModule`, `DomainModule`, `ViewModelModule` |
| **Observability** | Log score events at debug; Firebase Performance traces for recomposition count |
| **Crash reporting** | Sentry / Firebase Crashlytics with breadcrumbs for last N events |
| **Feature flags** | Remote config for interval ranges, player count, animation toggles |
| **Accessibility** | `contentDescription` on rank trends; semantic headings for hero |
| **Offline** | Show stale data with banner; queue is N/A if server-authoritative |
| **ProGuard** | Keep rules for `LeaderboardEntry`, kotlinx serialization if added |
