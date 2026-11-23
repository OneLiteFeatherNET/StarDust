## 1. Tests first (F.I.R.S.T.)

- [ ] 1.1 Write a failing test: three changes to one player produce one pending write.
- [ ] 1.2 Write a failing test: a manual `flush()` writes all dirty players in one transaction.
- [ ] 1.3 Write a failing test: quit flushes that player immediately, without waiting for the periodic flush, and removes it from the dirty map.
- [ ] 1.4 Write a failing test: disable flushes everything before the writer closes.
- [ ] 1.5 Write a failing test: a failing write is retried with the expected backoff sequence from an injected clock.
- [ ] 1.6 Write a failing test: a failed write increments the failure metric and logs (asserted through a captured appender).
- [ ] 1.7 Write a failing test: a full retry queue merges entries for the same player instead of dropping them.

## 2. Dirty tracking and flush

- [ ] 2.1 Implement the dirty map and coalescing.
- [ ] 2.2 Implement the periodic flush with JDBC batching (`hibernate.jdbc.batch_size`).
- [ ] 2.3 Implement the immediate flush on quit and the flush on disable.

## 3. Failure handling

- [ ] 3.1 Implement the bounded retry queue and exponential backoff.
- [ ] 3.2 Replace the exception swallowing in `saveOrUpdate` and `delete` with logging and the failure metric.
- [ ] 3.3 Export `stardust.persistence.failures` through the OpenTelemetry meter.

## 4. Configuration

- [ ] 4.1 Add `persistence.flush-interval` (default `5s`) to `config.yml` and read it at startup.
- [ ] 4.2 Document `hibernate.jdbc.batch_size` in the default `hibernate.cfg.xml` with the value 100.
- [ ] 4.3 Update the configuration reference page under docs/reference/ with the new keys.

## 5. Verification

- [ ] 5.1 Run the targeted unit tests with `heavy ./gradlew test --max-workers=2 --tests '*PlayerWrite*'`.
- [ ] 5.2 Load test against a staging database to confirm at least 1000 rows per second with a 50 ms round trip.

## 6. Pull request

- [ ] 6.1 Open the code pull request "perf(persistence): batch and coalesce player writes".
