## Context

`DefaultStardustPlayerService` (`src/main/java/net/onelitefeather/stardust/service/DefaultStardustPlayerService.java`) opens a Hibernate session and runs a JPQL query in `findById`. `findById` and `saveOrUpdate` are called from listeners on the server thread: `PlayerVanishListener` (block break, place, interact, entity target, damage, pickup, drop, consume, death), `VanishSilentContainerFeature`, `BukkitPlayerVanishService.isVanished`, `PlayerConnectionListener` and several commands.

A round trip of 50 to 58 ms per call means a single event can block the tick for tens of milliseconds, and a burst of events can block it for seconds.

## Goals / Non-Goals

Goals:
- No database I/O on the server thread for online players.
- Reads of a player's data never overtake that player's pending writes.
- Clean shutdown: no write is lost when the plugin disables.

Non-goals:
- Making offline lookups fast (they stay synchronous and are used only by commands that run off the server thread, see `async-player-repository-api`).
- Cross-server consistency beyond the versioned handoff guard (see `guard-player-data-handoff`).

## Decisions

1. Decorator, not replacement. `CachingStardustPlayerService` wraps the existing `StardustPlayerService`. Existing callers keep compiling; the cache is switched on in one place.
2. Cache key and value. `ConcurrentHashMap<UUID, Optional<StardustPlayer>>`. `Optional.empty()` records "no row exists" so that a missing player does not trigger a query on every event.
3. Load on pre-login. `AsyncPlayerPreLoginEvent` runs off the server thread, so the database read is allowed there. It is registered at `MONITOR` and runs only when the result is `ALLOWED`, so a denied login never loads data.
4. Ordered reads. All reads for a player go through the single writer executor `stardust-db-writer`. A read queued after a write therefore sees that write, without locks on the main thread.
5. Write-through. A change updates the map immediately, so server-thread reads see it at once; the database write is queued on the same executor.
6. Invalidation on quit. `PlayerQuitEvent` at `MONITOR` queues a final write and removes the entry after that write completes.
7. Shutdown order. `onDisable` drains the writer and closes the decorator before `SessionFactory.close()`. The decorator implements `AutoCloseable` so that the order is explicit in a try-with-resources or a single close call.

## Risks / Trade-offs

- Stale data on a second server. A player who moves between servers can read data cached on the old server until the cache is refreshed. Mitigated by the versioned handoff guard in `guard-player-data-handoff`, which rejects a stale write instead of overwriting, and by the quit-time flush.
- Memory. The map holds only online players, so its size is bounded by the number of online players.
- Lost writes on crash. A crash before the queued write runs loses that write. `batch-player-writes` narrows this window with a bounded, periodic flush and retry.

## Testing (F.I.R.S.T.)

- The writer executor and the clock are injected, so tests use a direct executor and a fixed `Clock`. No sleeps.
- MockBukkit provides player and event fixtures; no database is needed because the repository is an in-memory fake.
