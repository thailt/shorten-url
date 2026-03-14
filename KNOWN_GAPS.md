# KNOWN_GAPS.md

## Purpose
Track mismatches between:
- intended design
- documented behavior
- implemented behavior
- production-grade expectations

---

## Resolved gap: custom alias removed from request contract
### Observed
- `AliasCreateRequest` no longer exposes `alias`
- default create flow always uses generated alias keys

### Impact
- request contract now matches implemented behavior better
- removes confusion for API consumers and coding agents

### Follow-up
- ensure any remaining external examples or clients are updated accordingly

---

## Resolved gap: default create flow now persists before acknowledge
### Observed
- default create flow now saves alias to MySQL synchronously
- cache/redis/bloom are populated after DB persistence succeeds

### Impact
- create semantics are easier to reason about
- durability model is clearer than the old buffered default

### Follow-up
- legacy buffered-write components still exist in the repo and should be clearly isolated or removed later

---

## Gap 3: Uniqueness enforcement is more implicit than explicit
### Observed
- key strategy implies very low collision risk
- create flow itself does not visibly perform explicit duplicate handling in primary path

### Impact
- guarantees depend on surrounding assumptions
- future strategy changes may break expectations silently

### Recommended action
- document exact uniqueness contract
- add collision test coverage

### Status update
- DB uniqueness was strengthened by adding a unique constraint on `alias`

---

## Gap 4: Bloom filter lifecycle is operationally important but lightly documented
### Observed
- startup warms Bloom filter from DB in batches
- miss path depends on bloom behavior

### Impact
- startup cost grows with dataset size
- operational debugging can be confusing without metrics

### Recommended action
- add bloom warmup notes + metrics
- document acceptable stale/warmup behavior

---

## Gap 5: Multi-instance semantics are not yet explicit
### Observed
- current write-behind queue is in-process memory
- docs do not define behavior across multiple app instances

### Impact
- scaling assumptions may be misunderstood
- durability and consistency may vary by deployment topology

### Recommended action
- document single-instance vs multi-instance assumptions clearly
