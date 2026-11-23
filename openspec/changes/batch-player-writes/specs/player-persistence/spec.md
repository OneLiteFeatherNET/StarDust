## ADDED Requirements

### Requirement: Changes to a player are coalesced

The plugin SHALL keep at most one pending write per player, so that several changes to the same player between two flushes result in a single write.

#### Scenario: Repeated flag toggles

- **WHEN** the same player's flags are changed three times before the next flush
- **THEN** one write containing the latest state is performed

### Requirement: Periodic flush in one transaction

The plugin SHALL flush dirty players periodically at the interval set by `persistence.flush-interval` (default `5s`), in one database transaction, using JDBC batching.

#### Scenario: Periodic flush

- **WHEN** the flush interval elapses and dirty players exist
- **THEN** all dirty players are written in one transaction

#### Scenario: Interval configured by the server owner

- **WHEN** the server owner sets `persistence.flush-interval` to `10s`
- **THEN** the flush runs every 10 seconds after the next restart

### Requirement: Immediate flush on quit

The plugin SHALL write a player's pending changes immediately when the player quits, as a single write that is not held back for the periodic flush, so that the latest data is available to the next server, which may already be loading the player.

#### Scenario: Server switch

- **WHEN** a player with pending changes quits
- **THEN** the changes are written before the player's cache entry is removed

### Requirement: Flush on disable

The plugin SHALL write all pending changes before the persistence service is closed when the plugin is disabled.

#### Scenario: Plugin disabled

- **WHEN** the plugin is disabled with pending changes
- **THEN** all pending changes are written before the `SessionFactory` is closed

### Requirement: Bounded queue

The plugin SHALL bound the memory used for pending and retried writes. When the retry queue is full, the plugin SHALL merge entries for the same player instead of dropping them.

#### Scenario: Retry queue full

- **WHEN** the retry queue is full and a new entry for a player already in the queue arrives
- **THEN** the two entries are merged and no entry is discarded

### Requirement: Failed writes are retried and reported

The plugin SHALL retry failed writes with exponential backoff, SHALL log each failure, and SHALL export a failure counter through OpenTelemetry instead of swallowing the exception.

#### Scenario: Database unavailable

- **WHEN** a flush fails because the database is unreachable
- **THEN** the write is retried with increasing delay, a failure is logged, and `stardust.persistence.failures` is incremented
