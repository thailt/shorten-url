# ARCHITECTURE.md

## System Summary
This service maps long URLs to short aliases and supports fast redirect lookup.

It combines:
- persistent storage in MySQL
- Redis caching
- in-memory LRU cache
- Bloom filter pre-check
- generated keys via Snowflake or NanoID strategies

## High-Level Diagram

```text
Client
  -> ShortenController / AliasRedirectController
    -> ShortenUrlService
      -> KeyGenerationService
      -> LRU Cache
      -> BloomFilterService
      -> Redis
      -> AliasDBService / AliasRepository
        -> MySQL
      -> WriteBehindBuffer
```

## Main Flows

### 1. Create Short URL
```text
POST /app/api/create
  -> ShortenController
    -> ShortenUrlService.createShortUrl()
      -> generate key
      -> build response
      -> enqueue write-behind task
      -> update in-memory cache
      -> update bloom filter
      -> update Redis cache
      -> return shortened URL
```

Important property:
- response can be returned before durable DB write is fully completed because persistence is buffered / asynchronous through `WriteBehindBuffer`.
- current write path is better described as eventually persisted rather than immediately durable.
- this means correctness and recovery behavior must be evaluated together with performance.

### 2. Redirect / Lookup Alias
```text
GET /{alias}
  -> AliasRedirectController
    -> ShortenUrlService.getAlias()
      -> check local LRU cache
      -> check Bloom filter
      -> check Redis cache
      -> check DB through AliasDBService
      -> cache result if found
      -> redirect
```

Important property:
- miss path is optimized by Bloom filter to avoid unnecessary DB/Redis work.

## Core Components

### Controllers
- `ShortenController` exposes create endpoint
- `AliasRedirectController` exposes redirect endpoint

### Service Layer
- `ShortenUrlService` is core orchestration interface
- `ShortenUrlServiceHImpl` is the primary implementation in current codebase
- `KeyGenerationService` abstracts key generation strategy
  - current default runtime strategy is `SnowflakeKeyGen` (`@Primary`)
- `AliasDBService` abstracts DB lookup/write concerns
- `WriteBehindBuffer` supports buffered persistence
- `BatchWriteService` / `AliasWriteTask` suggest asynchronous batch persistence behavior

### Persistence Layer
- `AliasRepository` is JPA repository for alias entity access
- MySQL is the source of truth for durable alias data

### Cache / Fast-path Layer
- in-memory LRU cache for fastest repeated lookups
- Redis as distributed/shared cache
- Bloom filter for existence pre-check

## Startup Behavior
At startup, the primary service rebuilds / warms Bloom filter content from DB aliases in batches.

Implication:
- startup behavior may become more expensive as alias volume grows
- changes here can affect boot time and memory usage

## Important Boundaries
- Controller should stay thin.
- Persistence remains source of truth even if Redis/LRU contains stale or missing data.
- Bloom filter is an optimization layer, not source of truth.
- Key generation strategy should remain replaceable.
- Write-behind buffering changes consistency characteristics and must be treated as a correctness-sensitive area.

## Consistency Model
Current architecture appears closer to:
- fast response on write path
- eventual persistence via buffered DB write path
- cache-first read optimization

This means create-path changes must consider:
- lost writes during crash windows
- delayed persistence
- duplicate alias risk
- cache/DB divergence windows
- restart/recovery behavior when cache knew data before DB durably stored it

## Sensitive Areas
- `ShortenUrlServiceHImpl`
- key generation strategy
- bloom filter false-positive / refresh behavior
- write-behind buffering and batch persistence
- startup preload logic
- redirect path latency behavior
