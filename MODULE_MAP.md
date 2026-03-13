# MODULE_MAP.md

## Main Modules / Packages

### controller
Responsibility:
- expose HTTP endpoints for create and redirect

Key files:
- `ShortenController`
- `AliasRedirectController`

### service
Responsibility:
- orchestrate alias creation, lookup, buffering, and DB interactions

Key files:
- `ShortenUrlService`
- `AliasDBService`
- `KeyGenerationService`
- `WriteBehindBuffer`
- `BatchWriteService`
- `AliasWriteTask`

### service/impl
Responsibility:
- primary implementation details for shortening flow and key generation

Key files:
- `ShortenUrlServiceHImpl`
- `BloomFilterService`
- `SnowflakeKeyGen`
- `NanoIdKeyGen`

### repository
Responsibility:
- JPA access to alias persistence

Key files:
- `AliasRepository`

### entity
Responsibility:
- persisted data structures

### dto
Responsibility:
- API request and response models

### config
Responsibility:
- Spring/application configuration

### cache / utils
Responsibility:
- local performance helpers and utility structures

## Dependency Direction
Expected direction:
- controller -> service
- service -> repository / cache / config
- repository -> entity

## Ownership Heuristic
- API behavior: controller + dto + service
- correctness and consistency: service + repository + write-behind path
- performance behavior: cache + bloom + Redis + lookup flow
