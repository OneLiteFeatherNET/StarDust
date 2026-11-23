## 1. Tests first (F.I.R.S.T.)

- [ ] 1.1 Write a failing H2 test: a loaded player carries the stored version, and a successful update advances it.
- [ ] 1.2 Write a failing H2 test: an update based on an old version is rejected as a conflict and the stored row is unchanged.
- [ ] 1.3 Write a failing H2 test: two inserts of the same UUID leave one row, and the second caller receives the existing player.
- [ ] 1.4 Write a failing unit test with fakes: a conflict replaces the cache entry with the repository state and discards the pending write.
- [ ] 1.5 Write a failing unit test with fakes: a conflict logs one WARN containing the UUID and both versions (captured appender).
- [ ] 1.6 Write a failing unit test with fakes: a conflict for player A leaves the cache entry and pending write of player B unchanged.
- [ ] 1.7 Write a failing unit test with fakes: a new player without id is inserted exactly once.

## 2. Domain model

- [ ] 2.1 Add `version` to `StardustPlayer`, set it on load and advance it after a successful write.

## 3. Versioned updates and conflicts

- [ ] 3.1 Use the entity `@Version` check on the update path and map the optimistic lock exception to a conflict result.
- [ ] 3.2 On conflict: load the current row, replace the cache entry, drop the pending write and log the WARN.
- [ ] 3.3 On insert, handle the unique-constraint violation on `minecraft_uuid` by loading the existing row.
- [ ] 3.4 Route the flush path from `batch-player-writes` through the same versioned update.

## 4. Documentation

- [ ] 4.1 Add Javadoc for the version accessor and for the conflict behaviour of `StardustPlayerService` updates. No configuration key, command or permission changes, so no page under docs/reference/ changes.

## 5. Verification

- [ ] 5.1 Run the targeted unit tests with `heavy ./gradlew test --max-workers=2 --tests '*PlayerDataHandoff*'`.
- [ ] 5.2 Staging test with two servers: move a player while the first server still has a pending write, and confirm the stale write is rejected and logged.

## 6. Pull request

- [ ] 6.1 Open the code pull request "fix(player): detect stale player data writes across servers".
