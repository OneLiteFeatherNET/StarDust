## Why

`StardustPlayerService` mixes two kinds of work: lookups of online players, which must be instant and safe on the server thread, and lookups of any player in the database, which must not block the server thread. Callers cannot tell them apart, so blocking calls end up on the main thread (for example in commands and in `StardustPluginTask`).

`StardustPluginTask` also calls Bukkit API from an asynchronous timer. Bukkit API is not thread-safe, so this is a correctness bug as well as a performance concern.

## What Changes

- Split `StardustPlayerService` into two types:
  - `PlayerSessions`: synchronous, memory only, covers online players, safe on the server thread.
  - `PlayerRepository`: returns `CompletableFuture`, is never called with `join()` on the server thread.
- Offline-player commands (for example player info) load asynchronously. Bukkit API calls happen after hopping back to the main thread through the scheduler.
- `StardustPluginTask` runs on the main thread and reads only from `PlayerSessions`.

Code pull request title: `refactor(api)!: split player data access into sessions and an async repository`

Commit type and scope: `refactor(api)` with `!`. BREAKING: this changes the public `net.onelitefeather.stardust.api` package. Server owner action: none for configuration; other plugins that depend on StarDust must update their code (see the migration note in design.md).

BREAKING CHANGE footer: "`StardustPlayerService` is replaced by `PlayerSessions` (online players, synchronous) and `PlayerRepository` (any player, asynchronous, returns `CompletableFuture`). Callers of `findById` and `saveOrUpdate` must choose one of the two."

## Impact

- Affected code: `StardustPlayerService` and its implementation, `PlayerInfoCommand`, `VanishCommand`, `FlightCommand`, `PlayerChatListener`, `VanishNoPacketListener`, `StardustPluginTask`, `BukkitPlayerVanishService`.
- Dependent plugins: must migrate (see design.md).
- Dependencies: requires `cache-online-player-data`, `store-player-flags-in-pdc` and `batch-player-writes`. Those changes make `PlayerSessions` the memory-backed view that this change exposes.

## Out of scope

- Changing the database schema or the setting keys.
- Cross-server handoff guard (`guard-player-data-handoff`).
- Migrating commands that do not touch player data.
