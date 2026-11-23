## 1. Tests first (F.I.R.S.T.)

- [ ] 1.1 Write a failing test: an update with a stale version is rejected and the player is reloaded, not overwritten.
- [ ] 1.2 Write a failing test: the message `player-updated <uuid> <version>` is parsed; malformed messages are ignored and logged.
- [ ] 1.3 Write a failing test: a received message for a held player with a newer version replaces the cache entry asynchronously.
- [ ] 1.4 Write a failing test: a received message for a player that is not held is ignored.
- [ ] 1.5 Write a failing test: with `sync.player-invalidation.enabled: false` no broker connection is created.
- [ ] 1.6 Write a failing test: publishing happens only after the commit has completed.

## 2. Optimistic locking

- [ ] 2.1 Add the version check to the update path (`WHERE uuid = :uuid AND version = :expected`).
- [ ] 2.2 Report a conflict through the failure metric and reload the row.

## 3. Broker integration

- [ ] 3.1 Add a broker client abstraction with a Valkey pub/sub implementation.
- [ ] 3.2 Publish `player-updated <uuid> <version>` after commit.
- [ ] 3.3 Subscribe once and reload held players asynchronously on messages.
- [ ] 3.4 Reconnect with backoff; log and skip publishing while the broker is unreachable.

## 4. Configuration and documentation

- [ ] 4.1 Add `sync.player-invalidation.enabled` (default `false`) and the broker settings to `config.yml`.
- [ ] 4.2 Document the owner steps (broker URL, credentials, enabling per server) in the configuration reference under docs/reference/.

## 5. Verification

- [ ] 5.1 Run the targeted unit tests with `heavy ./gradlew test --max-workers=2 --tests '*PlayerInvalidation*'`.
- [ ] 5.2 Staging test with two servers and one broker: change a flag on A, confirm B reloads the player within a few seconds.

## 6. Pull request

- [ ] 6.1 Open the code pull request "feat(sync): invalidate cached player data across servers".
