## Context

Servers in the OneLiteFeather network share one database. Each server keeps a memory cache of online players. A player can join server B after changing data on server A; if server B cached the player earlier, B holds stale data until it is refreshed.

## Goals / Non-Goals

Goals:
- A server that holds a player's data learns about a committed change within about a second.
- A stale write is detected, not silently applied over newer data.
- Off by default; no network dependency unless enabled.

Non-goals:
- Strong consistency between servers. Invalidation is eventually consistent; the database remains the source of truth.
- Synchronising the PDC. The PDC is per server and is refreshed from the snapshot on join.
- Minecraft plugin messaging. It is not used for this, by OneLiteFeather rule.

## Decisions

1. Optimistic locking. `StardustPlayerEntity.version` is compared on update: `UPDATE ... WHERE uuid = :uuid AND version = :expected`. A zero-row result means a conflict; the writer reloads the row and reports the conflict through the failure metric from `batch-player-writes`. Changes are not silently overwritten.
2. Message format. Plain text `player-updated <uuid> <version>` on the channel `stardust.player-updated`. The version is included so that a receiver can skip a message for a version it already holds.
3. Broker choice. Two candidates:
   - Valkey/Redis pub/sub. Simple, low latency, one client library. Messages are not persisted; a server that is offline misses them, which is acceptable because it reloads on join. Recommended when the network already runs Valkey or Redis.
   - NATS core. Lightweight, subject-based, also not persisted. Recommended if the network adopts NATS for other services.
   Decision for this change: Valkey pub/sub, unless the network already standardises on NATS. The server owner confirms the choice before enabling.
4. Publish after commit. The message is sent only after the transaction commits, so a receiver that reloads sees the new row.
5. Receive. Each server subscribes once. On a message for a player it holds, it reloads that player asynchronously on the writer executor and replaces the cache entry if the loaded version is newer. Messages for players not held are ignored.
6. Off by default. Configuration key `sync.player-invalidation.enabled: false` (proposed). When disabled, no connection to the broker is opened and the optimistic lock still applies.
7. Failure handling. If the broker is unreachable, publishing is skipped and logged; the feature degrades to the behaviour without it. Reconnects use backoff.

## Migration and owner action

- Server owner: configure the broker URL and credentials in `config.yml` and set `sync.player-invalidation.enabled: true` on each server that should participate. No database schema change is needed beyond the existing `version` column.
- Developers: no API change.

## Risks / Trade-offs

- Pub/sub messages are lost while a receiver is offline; mitigated by reloading on join.
- A broker outage delays invalidation to the next join; the database still holds the correct data.
- Enabling the feature on some servers only gives partial benefit; the documentation must say so.

## Testing (F.I.R.S.T.)

- The broker is replaced by an in-memory fake that delivers messages on an injected executor. No network, no sleeps.
- The version check is tested with an in-memory repository that can return zero rows.
- Message parsing is tested with fixed strings; invalid messages are asserted to be ignored and logged (captured appender).
