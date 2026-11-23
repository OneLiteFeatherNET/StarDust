## ADDED Requirements

### Requirement: Domain model carries the entity version

The `StardustPlayer` domain model SHALL carry the `version` of the persisted row. A load SHALL set the version from the stored row, and a successful write SHALL advance it to the version stored by the database.

#### Scenario: Load sets the version

- **WHEN** a player row with version 7 is loaded
- **THEN** the `StardustPlayer` reports version 7

#### Scenario: Successful write advances the version

- **WHEN** a player with version 7 is updated and the update succeeds
- **THEN** the `StardustPlayer` reports the version now stored, 8

### Requirement: Updates are versioned

An update SHALL be applied only if the version it is based on equals the version stored in the database. An update based on an older version SHALL fail as a conflict and SHALL NOT overwrite the stored row.

#### Scenario: Matching version

- **WHEN** an update is based on version 7 and the stored version is 7
- **THEN** the update is applied

#### Scenario: Stale version

- **WHEN** an update is based on version 6 and the stored version is 7
- **THEN** the update fails as a conflict and the stored row is unchanged

### Requirement: Conflicts replace the cache with the database state

On a conflict the plugin SHALL replace the player's cache entry with the current database state, SHALL discard the conflicting write, and SHALL log a WARN message that names the player UUID and both versions. Writes for other players SHALL NOT be affected.

#### Scenario: Stale write on a held player

- **WHEN** a write for player P based on version 6 conflicts with stored version 7
- **THEN** the cache entry of P holds the stored row of version 7, the conflicting write is not persisted, and one WARN message names P and versions 6 and 7

#### Scenario: Other players unaffected

- **WHEN** a conflict occurs for player P
- **THEN** the cache entries and pending writes of all other players are unchanged

### Requirement: New players are inserted once

A player without a stored id SHALL be inserted once. If the insert fails because another server has inserted the same UUID first, the plugin SHALL load the existing row instead of failing.

#### Scenario: First login

- **WHEN** a player without a stored row logs in for the first time
- **THEN** one row is inserted for the player's UUID

#### Scenario: Concurrent first login on two servers

- **WHEN** the insert for a UUID fails because the row already exists
- **THEN** the existing row is loaded and used, and no error is raised to the player
