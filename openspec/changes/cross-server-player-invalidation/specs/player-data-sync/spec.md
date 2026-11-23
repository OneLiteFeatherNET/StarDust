## ADDED Requirements

### Requirement: Stale writes are detected by version

The plugin SHALL compare the stored `version` before applying an update to a player row. If the version does not match, the plugin SHALL NOT overwrite the row; it SHALL reload the row and report the conflict.

#### Scenario: Stale update

- **WHEN** a player's update carries a version older than the stored version
- **THEN** the stored row is kept, the player's data is reloaded, and the conflict is reported

### Requirement: Committed changes are announced

When sync is enabled, the plugin SHALL publish `player-updated <uuid> <version>` on the broker channel `stardust.player-updated` after the transaction commits.

#### Scenario: Flag change committed

- **WHEN** a player's data is committed with version 7
- **THEN** the message `player-updated <uuid> 7` is published after the commit

### Requirement: Servers holding a player reload it

A server that holds a player's data SHALL reload that player asynchronously when a `player-updated` message arrives with a newer version, and SHALL ignore messages for players it does not hold.

#### Scenario: Held player updated elsewhere

- **WHEN** server B holds player P at version 6 and receives `player-updated <P> 7`
- **THEN** B reloads P asynchronously and replaces the cached entry

#### Scenario: Player not held

- **WHEN** a server receives a message for a player it does not hold
- **THEN** the message is ignored

### Requirement: Off by default and owner-enabled

The plugin SHALL keep cross-server invalidation disabled unless the server owner sets `sync.player-invalidation.enabled` to `true`. When disabled, the plugin SHALL NOT open a broker connection.

#### Scenario: Default configuration

- **WHEN** the plugin starts with the default configuration
- **THEN** no broker connection is opened and no message is published

### Requirement: Plugin messaging is not used

The plugin SHALL NOT use Minecraft plugin messaging for player invalidation.

#### Scenario: Invalidation message sent

- **WHEN** a player update is announced to other servers
- **THEN** the message is sent through the configured broker, not through plugin messaging channels

### Requirement: Broker outages do not block the server

If the broker is unreachable, the plugin SHALL skip publishing, log the failure, and reconnect with backoff without blocking the server thread.

#### Scenario: Broker down

- **WHEN** the broker is unreachable during a publish
- **THEN** the publish is skipped and logged, and the server thread is not blocked
