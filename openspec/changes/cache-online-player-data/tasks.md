## 1. Tests first (F.I.R.S.T.)

- [ ] 1.1 Write a failing test: a cached player read on the server thread performs no repository call.
- [ ] 1.2 Write a failing test: a read queued after a write returns the written value.
- [ ] 1.3 Write a failing test: `AsyncPlayerPreLoginEvent` with a result other than `ALLOWED` loads nothing.
- [ ] 1.4 Write a failing test: `PlayerQuitEvent` queues a final write and then removes the cache entry.
- [ ] 1.5 Write a failing test: `close()` drains pending writes before the repository is closed.
- [ ] 1.6 Write a failing test: an offline lookup is not stored in the cache.

## 2. Caching decorator

- [ ] 2.1 Implement `CachingStardustPlayerService` with `ConcurrentHashMap<UUID, Optional<StardustPlayer>>`.
- [ ] 2.2 Route all reads and writes through the single-thread executor `stardust-db-writer`.
- [ ] 2.3 Implement `AutoCloseable.close()` that drains the writer.

## 3. Wiring

- [ ] 3.1 Load player data in `AsyncPlayerPreLoginEvent` at `MONITOR` when the result is `ALLOWED`.
- [ ] 3.2 Invalidate the entry on `PlayerQuitEvent` at `MONITOR`.
- [ ] 3.3 Close the decorator in `onDisable` before the `SessionFactory` closes.

## 4. Verification

- [ ] 4.1 Run the targeted unit tests with `heavy ./gradlew test --max-workers=2 --tests '*CachingStardustPlayerService*'`.
- [ ] 4.2 Run a manual check on a test server: join, break a block, quit, and confirm that no query runs on the server thread (timings from the watchdog are clean).

## 5. Pull request

- [ ] 5.1 Open the code pull request "perf(player): cache player data and keep database access off the main thread".
