## Context

With a 50 ms round trip, one connection performs about 20 writes per second when each write is its own transaction. The Survival server issued 466 to 590 queries per minute with two players, most of them writes or lookups. Writes are not on the server thread once `cache-online-player-data` is in place, but they still queue behind each other on `stardust-db-writer`, so a slow database delays persistence and increases the crash window.

## Goals / Non-Goals

Goals:
- Sustain several thousand rows per second on one connection with a 50 ms round trip.
- Bounded memory and bounded data loss (one flush interval).
- Failures are visible in logs and metrics.

Non-goals:
- Strong durability on every change (the cache is write-through, but persistence is deliberately delayed).
- Multiple writer threads (ordering per player is kept by one writer).

## Decisions

1. Dirty map. `ConcurrentHashMap<UUID, StardustPlayer>` of players whose state changed since the last successful flush. A second change replaces the first, so coalescing is automatic.
2. Periodic flush. A scheduled task on the writer executor flushes the dirty map every `persistence.flush-interval` (default `5s`, proposed key name). The interval is read once at startup, and the server owner can change it in `config.yml`.
3. Single transaction per flush. One session, one transaction, JDBC batching with `hibernate.jdbc.batch_size` (proposed value 100). Alternatively, a native upsert (`INSERT ... ON DUPLICATE KEY UPDATE` for MariaDB, `ON CONFLICT` for PostgreSQL). The batching option is preferred because it works for both databases through Hibernate without SQL dialect code; the upsert is kept as a fallback if batching does not reach the target throughput.
4. Immediate flush on quit. `PlayerQuitEvent` removes the player from the dirty map and writes that player at once. This is the hand-off point for a server switch.
5. Flush on disable. `onDisable` flushes the whole dirty map before the writer closes.
6. Bounded queue. The dirty map is bounded by the number of online players, plus the quit-time entries. Quit-time entries that fail are kept in a bounded retry queue (proposed size 1000); when it is full, the oldest entry for the same player is merged, not dropped.
7. Retry. Failed flushes are retried with exponential backoff (proposed 1 s, 2 s, 4 s ... capped at 60 s). Each failure is logged once per backoff step with the player count and exported as a counter `stardust.persistence.failures` with an OpenTelemetry meter.
8. Optimistic version. Each write increments the `version` column. A write whose version does not match the stored version is treated as a conflict and reported (see `cross-server-player-invalidation`).

## Throughput reasoning

- Unbatched: 1 transaction per row, 1 round trip per row, about 20 rows per second per connection at 50 ms.
- Batched: 100 rows per round trip, about 2000 rows per second per connection at 50 ms, before database work.

## Risks / Trade-offs

- A crash loses at most one flush interval (5 seconds by default) of changes that were not flushed on quit.
- Retries can delay later flushes if the database is down; the bounded queue limits memory during an outage.
- Server owners who lower the interval increase write load; the default is chosen to keep the rate low.

## Testing (F.I.R.S.T.)

- The scheduler is replaced by a manual trigger; tests call `flush()` directly. No sleeps and no wall-clock waits.
- The clock is injected for backoff computations.
- The repository is an in-memory fake that can be told to fail; failures are asserted through the metric and a captured appender.
