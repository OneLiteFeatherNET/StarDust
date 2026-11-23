## Why

After `cache-online-player-data`, the vanish listeners still ask the cache for flags on every event, and `BukkitPlayerVanishService.isVanished` still reads the database indirectly when the snapshot is not ready. Hot-path checks such as block break or damage are the most frequent calls, so each one should cost a memory read.

`BukkitPlayerVanishService.isVanished` also ANDs the database flag with a value in the player's persistent data container (PDC), which has a different meaning per server. The two sources can disagree after a server switch.

A player's PDC is saved in that server's world `playerdata` only. It does not travel between servers of the network. So the PDC is a fast per-server copy; the database stays the source of truth across servers.

## What Changes

- Hot-path flags (the flags that vanish listeners read) are read only from the PDC, under namespaced keys `stardust:<flag>`.
- On join, on the main thread, the flags from the asynchronously loaded snapshot are written into the PDC, overwriting any stale local value.
- On change, the PDC is set immediately and the database is persisted asynchronously.
- `BukkitPlayerVanishService.isVanished` no longer queries the database.
- If no snapshot exists for a player, the flags fall back to safe defaults (not vanished, default permissions) and a warning is logged.

Code pull request title: `perf(vanish): read player flags from the persistent data container`

Commit type and scope: `perf(vanish)`. Not breaking. No server owner action is needed.

## Impact

- Affected code: `BukkitPlayerVanishService`, `PlayerVanishListener`, `VanishSilentContainerFeature`, `PlayerConnectionListener`.
- Players see the same behaviour as before on one server. Across servers, the database value applies when the player joins the new server.
- Dependencies: requires `cache-online-player-data` (the snapshot must be loaded before join). Also used by `batch-player-writes` (persistence path).

## Out of scope

- Changing the set of flags or the database schema.
- Writing flags to the database in batches (`batch-player-writes`).
- Propagating changes to other running servers. Stale writes are rejected by `guard-player-data-handoff`.
