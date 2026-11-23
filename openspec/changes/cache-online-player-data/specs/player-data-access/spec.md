## ADDED Requirements

### Requirement: No database I/O on the server thread for online players

The plugin SHALL serve reads of online players' data from memory. A read for an online player on the server thread SHALL NOT call the database.

#### Scenario: Vanish check on a cached player

- **WHEN** a listener on the server thread reads the vanish flag of an online player whose data is cached
- **THEN** the value is returned from memory and no database query is executed

### Requirement: Asynchronous load on pre-login

The plugin SHALL load a player's data in `AsyncPlayerPreLoginEvent` at `MONITOR` priority, and SHALL do so only when the login result is `ALLOWED`.

#### Scenario: Allowed login

- **WHEN** a player's pre-login result is `ALLOWED`
- **THEN** the player's data is loaded asynchronously and cached before the join event runs

#### Scenario: Denied login

- **WHEN** a player's pre-login result is not `ALLOWED`
- **THEN** no player data is loaded

### Requirement: Reads are ordered after pending writes

The plugin SHALL route reads and writes for player data through a single writer executor, so that a read sees every write queued before it.

#### Scenario: Read after a queued write

- **WHEN** a setting is changed and a read for the same player is requested immediately afterwards
- **THEN** the read returns the changed value

### Requirement: Write-through cache with ordered asynchronous persistence

The plugin SHALL update the cache immediately on a change and SHALL persist the change asynchronously on the single-thread executor `stardust-db-writer`.

#### Scenario: Change made on the server thread

- **WHEN** a player's flag is changed on the server thread
- **THEN** the cache holds the new value at once and the database write is queued without blocking the server thread

### Requirement: Invalidation on quit

The plugin SHALL queue a final write and SHALL remove the player's cache entry on `PlayerQuitEvent` at `MONITOR` priority, after that write has completed.

#### Scenario: Player quits

- **WHEN** a cached player quits
- **THEN** the final changes are written and the cache entry is removed after the write completes

### Requirement: Flush and close on disable

The plugin SHALL drain the writer executor and close the caching service (`AutoCloseable`) in `onDisable` before the `SessionFactory` is closed.

#### Scenario: Plugin disabled with pending writes

- **WHEN** the plugin is disabled while writes are queued
- **THEN** all queued writes complete before the `SessionFactory` is closed

### Requirement: Offline lookups are not cached

The plugin SHALL NOT store results of lookups for players who are not online in the cache.

#### Scenario: Offline player lookup

- **WHEN** a command looks up a player who is not online
- **THEN** the result is returned without being added to the cache
