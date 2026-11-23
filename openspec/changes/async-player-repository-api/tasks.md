## 1. Tests first (F.I.R.S.T.)

- [ ] 1.1 Write a failing test: `PlayerSessions.get` returns the cached player and performs no repository call.
- [ ] 1.2 Write a failing test: `PlayerSessions.get` returns empty for an offline player.
- [ ] 1.3 Write a failing test: `PlayerRepository.find` completes on the injected executor, not on the calling thread.
- [ ] 1.4 Write a failing test: an offline-player command performs its Bukkit call on the main-thread executor.
- [ ] 1.5 Write a failing test: `StardustPluginTask` runs on the main thread and reads only from `PlayerSessions`.
- [ ] 1.6 Write a failing test: a failing repository future is logged through a captured appender.

## 2. New API types

- [ ] 2.1 Add `PlayerSessions` in `net.onelitefeather.stardust.api` (synchronous, memory only).
- [ ] 2.2 Add `PlayerRepository` in `net.onelitefeather.stardust.api` (`CompletableFuture`).
- [ ] 2.3 Remove `StardustPlayerService` and update its implementation to the two types.

## 3. Callers

- [ ] 3.1 Migrate `PlayerInfoCommand` to `PlayerRepository` with a hop to the main thread for Bukkit calls.
- [ ] 3.2 Migrate `VanishCommand`, `FlightCommand`, `PlayerChatListener` and `VanishNoPacketListener` to `PlayerSessions`.
- [ ] 3.3 Move `StardustPluginTask` to the main thread and to `PlayerSessions.online()`.

## 4. Migration and documentation

- [ ] 4.1 Write the migration note (the list in design.md) into the release notes and into docs/ for dependent plugin developers.
- [ ] 4.2 Update the API reference page under docs/reference/ with the two types.

## 5. Verification

- [ ] 5.1 Run the targeted unit tests with `heavy ./gradlew test --max-workers=2 --tests '*PlayerSessions*' --tests '*PlayerRepository*' --tests '*StardustPluginTask*'`.
- [ ] 5.2 Check with a grep that no `join()` or `get()` on a `PlayerRepository` future appears in listener or task code.

## 6. Pull request

- [ ] 6.1 Open the code pull request "refactor(api)!: split player data access into sessions and an async repository".
