## Why

On the Survival server, `DefaultStardustPlayerService.findById` runs one JPQL query per call, and it is called synchronously on the server thread. Block break, block place, interact, damage, pickup, drop, consume and death listeners all read the vanish state, so each event waits for a database round trip.

Measured on 2026-11-23 with two players: the database round trip took 50 to 58 ms, and the server issued 466 to 590 queries per minute, which is 23 to 29 seconds of database time per minute. In one stall minute there were 896 queries (43 seconds of database time). The tick took 43.5 seconds, the watchdog dumped every stack in `findById`, and both players were kicked. CPU was low during the stall, so the cause is I/O wait, not computation.

A fix exists on the unmerged branch `perf/player-cache`. This change records that fix as requirements so that it can be reviewed, tested and merged on its own.

## What Changes

- Add a `CachingStardustPlayerService` decorator that keeps online players in a `ConcurrentHashMap<UUID, Optional<StardustPlayer>>`.
- Load a player's data asynchronously in `AsyncPlayerPreLoginEvent` at `MONITOR`, and only when the login result is `ALLOWED`.
- Read through the single writer executor so that a read sees all queued writes first.
- Use a write-through cache with asynchronous persistence on a single-thread executor named `stardust-db-writer`.
- Invalidate the cache entry on `PlayerQuitEvent` at `MONITOR`.
- On disable, flush the writer and close the decorator (`AutoCloseable`) before the `SessionFactory` closes.
- Do not cache offline lookups.

Code pull request title: `perf(player): cache player data and keep database access off the main thread`

Commit type and scope: `perf(player)`. Not breaking. No server owner action is needed.

## Impact

- Affected code: `DefaultStardustPlayerService`, the new caching decorator, `PlayerConnectionListener`, the plugin's `onDisable`.
- Affected players: the server thread no longer waits on the database for online players.
- Dependencies: none. This is the base change for `store-player-flags-in-pdc`, `batch-player-writes`, `async-player-repository-api` and `guard-player-data-handoff`.

## Out of scope

- Moving hot flags into the PDC (`store-player-flags-in-pdc`).
- Batching writes or changing the write schedule (`batch-player-writes`).
- Changing the public API (`async-player-repository-api`).
- Pushing cache updates between servers. Stale writes are rejected by `guard-player-data-handoff` instead.
- Changing the database schema or configuration files.
