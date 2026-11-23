## ADDED Requirements

### Requirement: Hot-path flags are read from the persistent data container

The plugin SHALL read the flags used by the vanish listeners only from the player's persistent data container (PDC), under keys in the namespace `stardust` (`stardust:<flag>`), and SHALL NOT query the database on that path.

#### Scenario: Block break by a vanished player

- **WHEN** a vanished player breaks a block
- **THEN** the vanish flag is read from the PDC and no database query is executed

#### Scenario: Vanish check does not query the database

- **WHEN** `isVanished` is called for an online player
- **THEN** the result is computed from the PDC only

### Requirement: Join copies the loaded snapshot into the PDC

On join, the plugin SHALL copy the flags from the snapshot loaded in pre-login into the PDC on the main thread, overwriting any value already stored there.

#### Scenario: Stale local value

- **WHEN** a player joins and the PDC holds a `stardust:vanished` value that differs from the loaded snapshot
- **THEN** the PDC value is replaced by the snapshot value

### Requirement: Flag changes update the PDC first and persist asynchronously

When a flag changes, the plugin SHALL set the PDC value immediately and SHALL persist the change to the database asynchronously.

#### Scenario: Player is vanished

- **WHEN** an operator vanishes a player
- **THEN** the PDC value is set on the server thread and the database write runs asynchronously

### Requirement: Database remains the source of truth across servers

The plugin SHALL treat the database value as authoritative when a player joins a server; the PDC is a per-server copy and SHALL NOT override the database when the player arrives.

#### Scenario: Player switches servers

- **WHEN** a player who was vanished on another server joins this server
- **THEN** the vanish flag on this server equals the database value

### Requirement: Fail-safe defaults when the snapshot is missing

If no snapshot exists for an online player, the plugin SHALL use the defaults "not vanished" and the default permissions, and SHALL log a warning that includes the player's UUID.

#### Scenario: Snapshot missing

- **WHEN** a player is online and no snapshot is available
- **THEN** the player is treated as not vanished and a warning is logged once for that player
