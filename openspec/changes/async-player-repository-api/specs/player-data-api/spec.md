## ADDED Requirements

### Requirement: Player data access is split by thread safety

The public API SHALL offer `PlayerSessions` for synchronous, memory-only access to online players, and `PlayerRepository` for access to any player through `CompletableFuture`. `StardustPlayerService` SHALL be removed.

#### Scenario: Server-thread lookup of an online player

- **WHEN** a caller on the server thread asks `PlayerSessions` for an online player
- **THEN** the player is returned synchronously and no database call is made

#### Scenario: Lookup of an offline player

- **WHEN** a caller asks `PlayerRepository` for a player by UUID
- **THEN** a `CompletableFuture` is returned that completes without blocking the calling thread

### Requirement: Repository results are never joined on the server thread

Code that runs on the server thread SHALL NOT call `join()` or `get()` on a future returned by `PlayerRepository`.

#### Scenario: Command handler

- **WHEN** a command handler needs an offline player
- **THEN** it chains the future with an executor that runs on the main thread for any Bukkit API call

### Requirement: Bukkit API is called on the main thread only

The plugin SHALL call Bukkit API only on the main thread. `StardustPluginTask` SHALL run on the main thread and SHALL read only from `PlayerSessions`.

#### Scenario: Periodic task

- **WHEN** the periodic player task runs
- **THEN** it runs on the main thread and reads online players from `PlayerSessions` without database access

### Requirement: Migration note for dependent plugins

The release notes SHALL list, for each removed method of `StardustPlayerService`, its replacement in `PlayerSessions` or `PlayerRepository`.

#### Scenario: Dependent plugin upgrades

- **WHEN** a dependent plugin developer reads the release notes
- **THEN** every removed method has a listed replacement
