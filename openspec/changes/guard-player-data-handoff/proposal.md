## Why

StarDust runs on the Survival Paper server, and maybe on one more Paper server later. It never runs on lobbies. A player is online on one server at a time, and that server is the only writer for the player while the player is online. It loads the player's data on pre-login (`cache-online-player-data`) and writes each change as it happens, in order, through the single writer.

A server switch breaks this ownership for a moment. Velocity connects the player to the new server before it disconnects from the old one. The new server can therefore load the player before the old server's last write has committed, and it caches an older copy. An administrator can also change a player who is online on another server. Without protection, the stale copy later overwrites newer data, and nothing reports it.

This change makes every write carry the version it was based on. A write based on an old version is rejected and the database state wins. No message broker is involved: the database already holds the version, so correctness does not depend on messages arriving.

## What Changes

- The domain model `StardustPlayer` carries the entity `version` from `StardustPlayerEntity` (already mapped with `@Version`). A load sets it, and a successful write advances it.
- Updates are versioned. An update whose version does not match the stored row fails as a conflict instead of overwriting the row.
- On a conflict the cache entry is replaced with the current database state, the conflicting write is dropped, and a WARN log names the player UUID and both versions. Writes for other players are not affected.
- A new player (no id yet) is inserted once. If another server inserts the same UUID first, the existing row is loaded instead of the insert failing.

Code pull request title: `fix(player): detect stale player data writes across servers`

Commit type and scope: `fix(player)`. Not breaking: the change adds a version accessor and changes behaviour only where a stale write would otherwise overwrite newer data. No server owner action: no configuration key, no `hibernate.cfg.xml` change, no database schema change (the `version` column exists) and no new infrastructure.

## Impact

- Affected code: `StardustPlayer` (version field), `StardustPlayerEntity` (version mapping, unchanged), `DefaultStardustPlayerService` (versioned update, conflict handling, insert race), the cache from `cache-online-player-data`, and the flush path from `batch-player-writes`.
- Players: a server switch can drop changes made on the new server in the seconds between its load and a conflicting write. The database value is kept, as decided by the maintainer.
- Dependencies: requires `cache-online-player-data`. The flush path of `batch-player-writes` must use the same versioned update.

## Out of scope

- A message broker of any kind (Redis, Valkey, Dragonfly, NATS) and CloudNet channel messages. See design.md.
- Minecraft plugin messaging.
- Making the new server wait for the old server's last write (a database lease). Recorded as a possible future step in design.md.
- Synchronising the PDC between servers (see `store-player-flags-in-pdc`).
- Pushing cache updates to other servers. Stale writes are rejected; stale reads are not refreshed proactively.
- Changing the public API beyond the version accessor.
