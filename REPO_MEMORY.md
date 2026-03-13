# REPO_MEMORY.md

## Purpose
Curated reusable lessons and non-obvious facts for this repository.
Do not use this file as a raw work log.

## Known Important Facts
- Redirect path is latency-sensitive and uses multiple lookup layers.
- Bloom filter is used to short-circuit obvious misses.
- Redis is a cache layer, not durable truth.
- MySQL is the intended durable storage layer.
- Create flow currently uses buffered/write-behind persistence semantics.
- The current primary create flow does not honor user-provided custom alias even though the request DTO still exposes such a field.

## Known Sharp Edges
- A successful create response may not mean DB persistence already completed.
- Startup bloom preload scans aliases from DB in batches.
- Cache + bloom + DB layering means lookup bugs can hide in more than one place.
- Alias generation and persistence strategy must be reasoned about together, not separately.
- The system can temporarily expose alias knowledge in cache/bloom/redis before DB durability is achieved.
- Uniqueness safety is strategy-driven; the primary create flow does not visibly enforce collision handling in its own path.

## Recurring Review Heuristics
- If redirect behavior is wrong, inspect cache order and bloom logic first.
- If alias exists in response but not DB, inspect write-behind path.
- If 404 behavior changes unexpectedly, inspect bloom filter assumptions.
- If performance changes, ask whether hit-path or miss-path was altered.

## Good Questions For Future Changes
- Is this layer authoritative or only an optimization?
- What consistency window does this change create?
- What happens on restart or partial failure?
- What evidence shows alias uniqueness is still safe?
