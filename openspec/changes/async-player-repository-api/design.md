## Context

`StardustPlayerService` (`src/main/java/net/onelitefeather/stardust/api/StardustPlayerService.java`) declares `findAll(int)`, `findOnlinePlayers()`, `findById(UUID)` and `saveOrUpdate(StardustPlayer)`. Its implementation `DefaultStardustPlayerService` runs database queries synchronously, and `findOnlinePlayers()` calls `findById` for each online player, so it is a database call per player on the caller's thread.

Only the online-player path can be served from memory after the earlier changes. The API must make that difference visible in the type, so that a caller cannot accidentally block the server thread.

## Goals / Non-Goals

Goals:
- A type whose methods are safe on the server thread (`PlayerSessions`).
- A type whose methods never block when called from the server thread (`PlayerRepository`, returns `CompletableFuture`).
- No Bukkit API call from an asynchronous thread.

Non-goals:
- Keeping the old method names working through deprecation. The package is still at a pre-1.0 stage, and a clean split is simpler to reason about than a shim that hides blocking calls.

## Decisions

1. `PlayerSessions` (synchronous). Methods: `Optional<StardustPlayer> get(UUID)` (memory only, returns empty when the player is not online), `Collection<StardustPlayer> online()`, and `void update(StardustPlayer)` (write-through to the cache; persistence is queued). It does not touch the database.
2. `PlayerRepository` (asynchronous). Methods: `CompletableFuture<Optional<StardustPlayer>> find(UUID)`, `CompletableFuture<List<StardustPlayer>> findAll(int limit)`, `CompletableFuture<Void> save(StardustPlayer)`. A static analysis or review rule forbids `join()` and `get()` on these futures in code that runs on the server thread.
3. Hop back for Bukkit calls. Offline-player commands run `repository.find(uuid).thenAcceptAsync(player -> ..., mainThreadExecutor)`, where `mainThreadExecutor` is a `java.util.concurrent.Executor` that schedules work with the Bukkit scheduler. The executor is injected, so tests can run it directly.
4. `StardustPluginTask`. The timer that today runs asynchronously is moved to the main thread. It reads only `PlayerSessions.online()`, so it performs no database I/O and no blocking.
5. Migration path for dependent plugins:
   - Replace `findById(uuid)` on the server thread with `PlayerSessions.get(uuid)` when the player is online; the call returns empty when the player is offline.
   - Replace `findById(uuid)` for any player with `PlayerRepository.find(uuid)` and handle the result in a future.
   - Replace `saveOrUpdate(player)` with `PlayerSessions.update(player)` for online players, or `PlayerRepository.save(player)` for others.
   - Replace `findOnlinePlayers()` with `PlayerSessions.online()`.

## Risks / Trade-offs

- Breaking change for dependent plugins: each caller must choose a type. Mitigated by the migration list above and a release note.
- `PlayerSessions.get` returns empty for an offline player where the old call returned data. This is intended and must be stated in the release note.
- Futures can hide exceptions; `PlayerRepository` completes exceptionally and the callers must log the exception in `exceptionally`.

## Testing (F.I.R.S.T.)

- `PlayerSessions` is tested without a database; it is a pure in-memory class.
- `PlayerRepository` is tested with an in-memory fake; no database and no sleeps.
- The main-thread executor is injected; tests use a direct executor and assert on the result, not on timing.
- A test asserts that `StardustPluginTask` makes no Bukkit call from a non-main thread (MockBukkit `isPrimaryThread` check).
