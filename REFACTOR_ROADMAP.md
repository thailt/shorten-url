# REFACTOR_ROADMAP.md

## Goal

Refactor `shorten-url` in a way that improves correctness and clarity first, then performance confidence, without losing the educational value of the current architecture.

---

## 1. Context

### Problem
Current implementation is interesting and fast-looking, but some key behaviors are implicit or risky:
- create API acknowledges before durable persistence
- custom alias contract is unclear
- uniqueness is strategy-driven but not explicitly enforced end-to-end
- cache / bloom / DB layers are helpful but increase reasoning complexity

### Refactor principle

```text
First make behavior explicit and correct.
Then make optimization boundaries clear.
Then optimize safely.
```

This means:
- correctness before cleverness
- explicit semantics before hidden trade-offs
- observability before deeper optimization

---

## 2. Recommended Refactor Direction

### Short summary
Move from:
- one service with mixed responsibilities and implicit trade-offs

toward:
- clearer write-path semantics
- clearer read-path boundaries
- stronger persistence/uniqueness contract
- documented and measurable performance optimizations

---

## 3. Refactor Phases

## Phase 0 — Freeze Semantics

### Goal
Decide what the system *means* before changing internals.

### Tasks
1. Decide custom alias removal policy:
   - remove from request DTO
   - or formally deprecate it before removal

2. Decide create acknowledgment policy:
   - **Option A:** acknowledge only after DB persistence succeeds
   - **Option B:** keep buffered ack, but document it as eventual durability

3. Decide uniqueness policy:
   - key generation only
   - DB unique constraint
   - both together

### Output
- updated API contract
- small ADR or documented decision note

### Recommendation
For this repo, I recommend:
- **remove/deprecate custom alias from the contract**
- **switch to synchronous DB write first** if the goal is correctness and clean mental model
- **add DB-level uniqueness on alias** even if Snowflake is the primary generator

Reason:
this makes the system easier to reason about, easier to test, and safer to evolve.

---

## Phase 1 — Separate Correctness Path from Optimization Path

### Goal
Make the write and read paths easier to understand.

### Current issue
`ShortenUrlServiceHImpl` currently owns too much:
- alias generation
- response construction
- cache writes
- bloom updates
- persistence handoff
- startup warmup logic indirectly depends on surrounding concerns

### Refactor target
Split responsibilities into clearer components.

Suggested structure:

```text
ShortenUrlService
  -> AliasCreationService
     -> AliasGenerator
     -> AliasPersistenceService
     -> AliasCacheWriter
  -> AliasLookupService
     -> LocalCacheLookup
     -> BloomGuard
     -> RedisLookup
     -> AliasRepositoryLookup
  -> BloomWarmupService
```

### Benefits
- easier unit testing
- easier to reason about failure points
- easier to swap sync write vs buffered write
- easier for coding agents to work in bounded areas

---

## Phase 2 — Make Persistence Semantics Explicit

### Goal
Eliminate ambiguity around durability.

### Two viable modes

#### Mode A — Correctness-first synchronous write
Flow:
- generate alias
- persist to DB
- populate cache/bloom/redis after success
- return response

Best when:
- this repo is primarily about clean architecture and correctness
- production semantics matter more than raw write throughput

#### Mode B — Buffered write with durable queue/outbox
Flow:
- generate alias
- persist event/task to durable outbox
- async worker flushes to alias table
- cache/bloom updates depend on durable boundary

Best when:
- you really want async write semantics
- you are willing to design recovery story properly

### Recommendation
For current repo maturity:
**choose Mode A first**.

Why:
- much simpler mental model
- easier debugging
- fewer ghost states
- easier test coverage

Buffered writes can come back later as an explicit optimization experiment.

---

## Phase 3 — Strengthen Data Integrity

### Goal
Push guarantees into the data model, not just Java code.

### Tasks
1. add unique constraint/index on `alias`
2. review whether `Alias` table naming / sequence strategy is intended long-term
3. define nullable vs non-nullable fields explicitly
4. define expiration semantics clearly

### Important note
Right now `Alias` entity has an index on `(alias, url)` but that is not the same as a uniqueness guarantee on `alias`.

### Recommendation
Add:
- unique constraint on `alias`
- not-null where appropriate

This gives a stronger safety net if generation strategy changes later.

---

## Phase 4 — Make Lookup Pipeline Explicit and Observable

### Goal
Keep read optimization, but make it debuggable.

### Suggested lookup contract

```text
lookup(alias)
  -> local cache
  -> bloom guard
  -> redis
  -> db
  -> hydrate caches on success
```

### Tasks
1. instrument cache hit/miss per layer
2. instrument bloom short-circuit count
3. instrument DB fallback rate
4. instrument redirect latency percentiles

### Why this matters
Without metrics, layered cache systems become story-driven rather than evidence-driven.

---

## Phase 5 — Isolate Bloom Filter Concerns

### Goal
Treat Bloom filter as a replaceable optimization, not hidden correctness logic.

### Tasks
1. move warmup logic out of `ShortenUrlServiceHImpl`
2. create dedicated `BloomWarmupService`
3. document startup behavior
4. expose warmup metrics
5. define what happens if Bloom is cold/unavailable

### Principle
System must remain correct if Bloom filter is missing or stale.
Bloom should only improve cost/latency.

---

## Phase 6 — Clean API Contract

### Goal
Make request/response semantics match code 100%.

### Tasks
1. remove/deprecate `alias` from request DTO
2. update API docs and examples accordingly
3. define error contract for validation failures clearly

### Recommendation
Do not keep half-supported API surface.
That creates continuous confusion for both humans and agents.

---

## Phase 7 — Testing Strategy Refactor

### Goal
Make the most important behavior provable.

### Minimum test matrix

#### Create path
- generates alias successfully
- persists alias successfully
- returns expected response
- handles duplicate/constraint conflict if relevant
- handles expiration defaulting correctly

#### Redirect path
- local cache hit
- bloom negative -> 404
- redis hit
- db hit -> cache hydrate
- not found -> 404

#### If buffered mode remains
- queue flush success
- queue flush failure
- process crash semantics documented/testable where possible

---

## 4. Practical Sequence I Recommend

If I were refactoring this repo myself, I would do it in this order:

### Step 1
Remove or formally deprecate the `alias` request field.

### Step 2
Add DB unique constraint on alias. ✅

### Step 3
Switch create path to synchronous DB persistence. ✅

### Step 4
Only populate Redis/LRU/Bloom after successful DB write. ✅

### Step 5
Split `ShortenUrlServiceHImpl` into:
- create service
- lookup service
- bloom warmup service

### Step 6
Add metrics around cache layers and bloom behavior.

### Step 7
If performance actually demands it, reintroduce buffered persistence as an explicit architecture experiment with proper recovery semantics.

---

## 5. Trade-off Summary

### Path I recommend: correctness-first refactor
Pros:
- easier reasoning
- fewer hidden failure modes
- better for learning and production realism
- easier for agents to modify safely

Cons:
- create throughput may be lower than current write-behind approach
- less flashy from a performance-demo perspective

### Path I do NOT recommend right now: optimize deeper first
Why not:
- current ambiguity is still too high
- adding more optimization before semantic clarity will compound confusion

---

## 6. Bottom Line

The next best evolution of this repo is **not** "more distributed systems tricks".
It is:

1. clarify semantics
2. strengthen guarantees
3. isolate optimizations
4. instrument behavior

Nói ngắn gọn:

**Biến repo từ “interesting but subtle” thành “clear, correct, and still fast enough.”**
