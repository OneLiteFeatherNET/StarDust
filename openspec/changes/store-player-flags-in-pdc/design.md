## Context

The vanish listeners (`PlayerVanishListener`, about 50 handlers) call `StardustPlayerService.findById` through the cache to read the flags `vanished`, `vanish_keep_inventory`, `vanish_allow_building`, `vanish_allow_pve` and `allow_flight`. The corresponding `StardustPlayer` accessors are `isVanished()`, `keepInv()`, `canBuild()`, `canAttack()` and `allowedFlight()`.

`BukkitPlayerVanishService` (`src/main/java/net/onelitefeather/stardust/service/BukkitPlayerVanishService.java`) already stores `vanished` in the PDC under `NamespacedKey(plugin, "vanished")`, and `isVanished` ANDs it with the database flag.

## Goals / Non-Goals

Goals:
- A hot-path flag check costs one PDC read on the server thread.
- The database remains the source of truth across servers.

Non-goals:
- Keeping flags current on servers where the player is not present.
- Changing how settings are stored in the JSON column.

## Decisions

1. Namespaced keys. Each flag is stored under `NamespacedKey("stardust", "<flag>")`, for example `stardust:vanished`. The existing `vanished` key already has this form when the plugin is named Stardust, so existing data stays readable.
2. Join sync. On `PlayerJoinEvent` (main thread), the flags are copied from the snapshot loaded in pre-login into the PDC. The copy overwrites the local value, so a stale value from an earlier visit cannot survive.
3. Change path. A change sets the PDC value first, then calls the persistence path asynchronously. The server thread never waits for the database.
4. `isVanished` without the database. It reads only the PDC. The AND with the database flag is removed; the database value is applied at join instead.
5. Missing snapshot. If the snapshot is absent (for example, load failed), the flags use fail-safe defaults: not vanished, default permissions. A warning is logged once per player with the player's UUID.

## Risks / Trade-offs

- A PDC write is lost if the player's world data is not saved (server crash). Acceptable: the database holds the last persisted value, and the join sync restores it.
- The fail-safe default for a missing snapshot can show a vanished player as visible for one login. The warning makes this visible; the alternative (blocking) is what this change removes.

## Testing (F.I.R.S.T.)

- Use MockBukkit players, whose PDCs are in memory. Each test creates its own player.
- The snapshot provider is a fake; no database and no sleeps.
- Assert the warning through a captured log appender, not by reading console output.
