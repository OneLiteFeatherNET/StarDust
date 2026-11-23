## Context

A player is online on one server at a time. That server owns the player's data while the player is online: it loads on pre-login and writes changes through the single ordered writer from `cache-online-player-data`. The database holds the `version` column of `StardustPlayerEntity`, which Hibernate increments on every update.

Velocity connects the player to the new server before it disconnects from the old one. For a short window, two servers hold the same player. The new server may load a row that the old server is about to update, and the old server's last write can commit after the new server has loaded. Both servers then write from different versions of the same row.

## Goals / Non-Goals

Goals:
- A write based on an old version never overwrites newer data silently.
- The conflict is visible in the log with the player UUID and both versions.
- The database is the only source of truth for resolving a conflict.
- No new infrastructure and no server owner action.

Non-goals:
- Making the new server see the old server's last write before it loads. That needs a lease or a broker and is deferred (see "Future option").
- Merging conflicting changes field by field. The database version wins and the conflicting write is dropped.
- Real-time refresh of idle caches on other servers.

## Decisions

1. Version on the domain model. `StardustPlayer` gets a `version` field. The loader sets it from the entity. A successful write sets it to the version the database now holds, which Hibernate has advanced.
2. Versioned update. The update uses the entity `@Version` check: the row is updated only where the stored version equals the version the write was based on. Hibernate raises its optimistic lock exception on a mismatch; the service maps that exception to a conflict result instead of letting it escape.
3. Database wins. On a conflict the service loads the current row, replaces the cache entry with it, discards the pending write for that player, and logs one WARN with the UUID, the version the write was based on and the stored version. Changes the new server made in the window are lost. This is accepted: the alternative is to overwrite newer data.
4. Scope of the conflict. The decision is per player. Other players' cache entries and pending writes are untouched.
5. Insert race. A new player has no `id`. The insert is attempted once. A unique-constraint violation on `minecraft_uuid` means another server inserted the row first; the service then loads that row and continues with it. No retry loop is needed.
6. Flush path. The versioned update is also used by the periodic and quit flush from `batch-player-writes`, so a batched write cannot bypass the check.

## Why no message broker

The guard needs no message between servers. Correctness comes from the version check in the database, which both servers already go through.

- StarDust runs on one, or at most two, Paper servers. A broker would be a third service to deploy, secure, monitor and keep available for two writers.
- Each player has one owner at a time, so there is no fan-out to serve. A message only makes an idle cache refresh sooner; the version check already prevents the harmful write.
- Hikari with MariaDB is already in the path of every write. Adding a second network dependency adds a failure mode without removing one.
- Pub/sub messages are not persisted. A server that is down misses them, so the design would still need a database check on join.

## Alternatives considered

- Valkey, Redis or Dragonfly pub/sub (publish `player-updated` after commit). Rejected: an extra service to operate for a problem the version check already solves, and messages are lost when a receiver is offline.
- NATS. Rejected for the same reasons; the network has no NATS deployment that StarDust could reuse.
- CloudNet channel messages. Deferred: they would couple StarDust to CloudNet's API and to its runtime, and StarDust should stay usable on a plain Paper server.
- Minecraft plugin messaging. Rejected by OneLiteFeather rule.

## Future option (out of scope)

A database lease column on the player row (owner server and a timestamp). The new server would wait for the lease to be released in the asynchronous pre-login step before loading. This would prevent the conflict instead of only detecting it. It needs its own change, because it adds a column (a schema change with server owner action) and a wait on the login path.

## Risks / Trade-offs

- A conflict drops the changes the new server made in the window between its load and the conflict. The WARN log makes this visible; the trade-off is accepted.
- A player who switches servers twice quickly can see more conflicts; each one is logged.
- The version check relies on the single writer per server being the only path that updates the row. Any new write path must use the versioned update.

## Testing (F.I.R.S.T.)

- Database behaviour is tested with the H2 test database, as in `DefaultStardustPlayerServiceTest`. Each test creates its own database and session factory.
- Conflict logging is asserted through a captured appender, not by reading console output.
- Cache replacement, pending-write discard and the insert race are unit-tested with an in-memory fake repository that can return a conflict or a unique-constraint violation on demand.
- No sleeps; the clock is injected where timestamps are involved.
