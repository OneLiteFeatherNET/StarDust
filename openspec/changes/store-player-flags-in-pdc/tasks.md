## 1. Tests first (F.I.R.S.T.)

- [ ] 1.1 Write a failing test: `isVanished` returns the PDC value and does not call the repository.
- [ ] 1.2 Write a failing test: join copies the snapshot flags into the PDC, overwriting a stale value.
- [ ] 1.3 Write a failing test: a flag change sets the PDC before the asynchronous persistence call.
- [ ] 1.4 Write a failing test: a missing snapshot yields the fail-safe defaults and logs a warning (asserted through a captured appender).
- [ ] 1.5 Write a failing test: PDC keys use the namespace `stardust`.

## 2. Flag storage

- [ ] 2.1 Introduce a `PlayerFlagKeys` helper that builds `stardust:<flag>` keys.
- [ ] 2.2 Implement join sync from the pre-login snapshot on the main thread.
- [ ] 2.3 Change `BukkitPlayerVanishService.setVanished` to set the PDC first and persist asynchronously.

## 3. Hot path

- [ ] 3.1 Remove the database read from `BukkitPlayerVanishService.isVanished`.
- [ ] 3.2 Switch `PlayerVanishListener` and `VanishSilentContainerFeature` to the PDC-backed flags.
- [ ] 3.3 Add the fail-safe default and the warning for a missing snapshot.

## 4. Verification

- [ ] 4.1 Run the targeted unit tests with `heavy ./gradlew test --max-workers=2 --tests '*BukkitPlayerVanishService*'`.
- [ ] 4.2 Manual check on a test server: vanish, switch to a second server, confirm the flag follows the player from the database.

## 5. Documentation

- [ ] 5.1 Update the vanish and flags page under docs/ if one exists for the flag behaviour.

## 6. Pull request

- [ ] 6.1 Open the code pull request "perf(vanish): read player flags from the persistent data container".
