## Why

Each server caches the data of its online players (`cache-online-player-data`). When a player changes data on one server and then joins another server, the second server can hold an older copy if it cached the player earlier. The database is the source of truth, but nothing tells the other servers that their copy is stale.

Today the only protection is that a player is usually loaded fresh on join. This change adds an explicit invalidation path so that servers that already hold a player's data learn about changes quickly, without a restart and without a database poll.

## What Changes

- Use the existing `version` column of `StardustPlayerEntity` for optimistic locking. A write whose version is older than the stored version is rejected, and the player's data is reloaded instead of overwritten.
- After a commit, publish `player-updated <uuid> <version>` on a message broker. The broker is Valkey (Redis-compatible) pub/sub or NATS; the choice is recorded in design.md.
- Servers that hold the player reload the player's data asynchronously when the message arrives.
- The whole feature is off by default and must be enabled by the server owner in configuration.
- OneLiteFeather rule: services must not use Minecraft plugin messaging for this.

Code pull request title: `feat(sync): invalidate cached player data across servers`

Commit type and scope: `feat(sync)`. Not breaking, because the feature is off by default and the default behaviour is unchanged. Server owner action is required to enable it: the broker address and credentials must be configured, and the feature flag must be switched on, on every server that should receive invalidations.

## Impact

- Affected code: `DefaultStardustPlayerService` (version check on write), the caching decorator from `cache-online-player-data`, the persistence path from `batch-player-writes`, plugin configuration.
- Infrastructure: a broker reachable from all game servers. Without it the feature stays off.
- Dependencies: requires `cache-online-player-data` and `batch-player-writes`.

## Out of scope

- Replacing the database as source of truth.
- Real-time synchronisation of the PDC (the PDC is per-server by design; see `store-player-flags-in-pdc`).
- Using Minecraft plugin messaging.
- Broker deployment itself; this change only consumes an existing broker.
