# ARCHITECTURE_REVIEW.md

## 1. Context

### Problem statement
This repository implements a URL shortening service with a strong emphasis on read latency and experimentation around key generation, caching, and buffered persistence.

### Current architecture style
- single Spring Boot application
- synchronous HTTP API surface
- layered caching on read path
- asynchronous batched persistence on write path

### Main quality attributes currently optimized
- low-latency redirect lookup
- fast write acknowledgment
- reduced DB pressure on read misses
- simple deployability for a study project

### Main quality attributes currently at risk
- legacy buffered-write code still present in repo and may confuse future changes
- consistency between cache and DB if semantics drift again
- uniqueness guarantees if strategy or persistence semantics drift
- observability around cache/bloom/write-path behavior is still limited

---

## 2. Proposed Architecture Understanding

### Short summary
The service is best understood as a **performance-oriented modular monolith** with:
- one API for create
- one API for redirect
- local + distributed cache on reads
- write-behind buffer on create
- MySQL as intended durable store
- Redis/Redisson for cache and Bloom filter support

### Component view

```text
Client
  -> HTTP Controllers
    -> ShortenUrlServiceHImpl
      -> KeyGenerationService (Snowflake default)
      -> WriteBehindBuffer -> Scheduled BatchWriteService -> MySQL
      -> LRU Cache
      -> Redis
      -> BloomFilterService
```

### Data flow

#### Create
- receive long URL
- generate alias key
- return API response quickly
- queue batched persistence
- warm Bloom/filter/cache layers immediately

#### Redirect
- check local cache
- check Bloom filter
- check Redis
- check MySQL fallback
- redirect when found

---

## 3. Key Decisions Observed

### Decision A: Use generated keys instead of deriving key from URL
Why it helps:
- avoids expensive duplicate-detection-by-content flow
- keeps alias generation cheap and decoupled from URL length/content

Trade-off:
- current implementation does not yet support stable user-provided alias behavior in primary flow

### Decision B: Move default create flow to DB-first persistence
Why it helps:
- clearer durability semantics
- easier reasoning and debugging
- cache is hydrated only after persistence succeeds

Trade-off:
- create latency may be higher than the old buffered default
- buffered-write code should now be treated as legacy/experimental unless explicitly revived

### Decision C: Use Bloom filter before deeper lookup
Why it helps:
- reduces unnecessary Redis/DB work on obvious misses
- especially useful for 404-heavy load tests

Trade-off:
- bloom data lifecycle and startup warmup become correctness-adjacent operational concerns

### Decision D: Use layered cache on redirect path
Why it helps:
- fits hot-read skewed workloads well
- improves redirect latency significantly

Trade-off:
- multiple truth-like layers increase debugging complexity

---

## 4. Cross-Cutting Concerns

### Security
Current repo appears focused more on performance than security hardening.
Areas to watch:
- URL validation / sanitization expectations
- alias enumeration risk if generation strategy changes
- cache poisoning / malformed redirect target handling
- config secret handling in local/dev paths

### Reliability
Main current reliability concern is create-path durability:
- response may be returned before successful DB flush
- scheduler failure or process crash can lose in-memory write queue

### Scalability
Current architecture can scale read-heavy workloads better than write-critical workloads because:
- read path has layered caching
- write path is still anchored to one application instance memory queue unless externally coordinated

### Observability
The repo includes Prometheus + Grafana, which is good.
But the most important business/operational metrics should be explicit:
- create acknowledged count
- write-behind queue depth
- batch flush success/failure count
- cache hit ratio
- bloom miss short-circuit count
- redirect fallback-to-DB rate

### Cost / Operations
This repo keeps operational complexity moderate:
- one app
- one MySQL
- one Redis
- monitoring sidecars

But behavior becomes less transparent because durability is partially deferred.

---

## 5. Risks and Mitigations

### Risk 1: Acknowledged create but lost before DB flush
Mitigation options:
- move to synchronous DB write for correctness-first mode
- or persist queue to durable log/outbox before ack
- or expose explicit durability semantics in API/ops docs

### Risk 2: Cache/DB divergence
Mitigation options:
- populate cache only after successful DB flush
- or add reconciliation process
- or make divergence an explicit accepted trade-off

### Risk 3: Custom alias contract drift
Mitigation options:
- either implement custom alias fully
- or remove/deprecate field from API contract

### Risk 4: Weak explicit uniqueness enforcement in create flow
Mitigation options:
- define uniqueness contract clearly
- add DB unique constraint if not already present
- add retry/duplicate handling strategy when needed

### Risk 5: Startup bloom warmup cost grows with dataset size
Mitigation options:
- add bounded warmup strategy
- snapshot bloom state if appropriate
- expose warmup metrics and startup impact clearly

---

## 6. Recommended Next Steps

### Immediate
1. make custom alias contract explicit: supported or not supported
2. document durability semantics in API/ops docs
3. verify DB-level uniqueness enforcement on alias
4. add metrics around write-behind queue and flush failures

### Near-term
1. add `KNOWN_GAPS.md`
2. add `DECISIONS.md` or ADRs for write-behind + bloom usage
3. add sequence diagrams for create and redirect
4. add test matrix for create durability and redirect lookup layers

### If evolving toward production-grade system
1. reconsider write acknowledgment semantics
2. define recovery story for in-flight buffered writes
3. define multi-instance coordination story
4. define cache invalidation/reconciliation policy

---

## 7. Bottom Line

This repo is not just a CRUD URL shortener.
It is a compact study of:
- key generation
- read optimization
- write-behind trade-offs
- practical cache layering

The most important mental model is:

**This system optimizes for fast response and fast lookup, but pays for that with more subtle consistency and durability behavior.**
