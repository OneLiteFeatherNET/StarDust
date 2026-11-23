## Why

Each change to a player's data currently results in one database transaction, and `DefaultStardustPlayerService.saveOrUpdate` and `delete` swallow exceptions. Throughput is limited by the round trip: at 50 ms per round trip, one connection performs about 20 sequential writes per second. A busy minute with several flag changes per player saturates that quickly, and a failed write leaves no trace in the logs or metrics.

Batching 100 rows per round trip gives thousands of rows per second on the same connection, and coalescing repeated changes to one player into one row means a flag toggled five times in a second costs one write.

## What Changes

- Track dirty state per player. Multiple changes to the same player are coalesced into one pending write.
- Flush dirty players periodically, by default every 5 seconds. The interval is a configuration key the server owner can change.
- Each flush runs in one transaction, using JDBC batching (`hibernate.jdbc.batch_size`) or an upsert.
- Flush a player immediately on quit, so that a server switch hands off the latest data. Flush all players on disable.
- Use a bounded queue. When the queue is full, the oldest pending entry for a player is merged, not dropped.
- Retry failed writes with exponential backoff. Log each failure and export it as a metric (OpenTelemetry is attached on the servers). Failures are no longer swallowed.

Code pull request title: `perf(persistence): batch and coalesce player writes`

Commit type and scope: `perf(persistence)`. Not breaking. Server owner action: optional. The flush interval can be tuned with a configuration key, and `hibernate.jdbc.batch_size` may need to be set in the server's `hibernate.cfg.xml` to reach the batch throughput.

## Impact

- Affected code: `DefaultStardustPlayerService` (`saveOrUpdate`, `delete`), the writer executor from `cache-online-player-data`, `hibernate.cfg.xml` defaults, the configuration loader.
- Players: no visible change; a crash can lose at most one flush interval of changes.
- Dependencies: requires `cache-online-player-data`. `store-player-flags-in-pdc` uses this change's persistence path.

## Out of scope

- Changing the database schema.
- Cross-server invalidation (`cross-server-player-invalidation`).
- Changing the public API (`async-player-repository-api`).
