# CONSTRAINTS.md

## Hard Constraints
- Do not break `POST /app/api/create` request/response contract without explicit note.
- Do not break `GET /{alias}` redirect semantics.
- Do not treat Bloom filter as authoritative truth.
- Do not move intended durable ownership away from MySQL unless intentionally redesigning the architecture.
- Do not casually change write-behind behavior without evaluating consistency risk.
- Do not assume `request.alias` is currently functional in the primary create flow.

## Sensitive Areas
- `src/main/java/com/trithai/utils/shortenurl/service/impl/ShortenUrlServiceHImpl.java`
- key generation components (`SnowflakeKeyGen`, `NanoIdKeyGen`)
- `WriteBehindBuffer`, `BatchWriteService`, `AliasWriteTask`
- bloom filter logic
- startup preload / initialization path
- Redis serialization and cache key behavior

## Risk Notes
- Create path may acknowledge before DB durability is guaranteed.
- Bloom filter mistakes can alter miss-path behavior.
- Cache layer changes can introduce stale-read or missing-data confusion.
- Key generation changes can introduce collision or compatibility problems.
- Startup alias preload can degrade startup time for large datasets.
- Request DTO and actual implementation are currently not fully aligned around custom alias support.

## Forbidden Actions
- Do not delete or bypass validation casually for API inputs.
- Do not mix unrelated cleanup/refactor into a functional change on hot paths.
- Do not change DB schema / entity semantics without noting data impact.
- Do not hardcode environment-specific secrets into committed config.
- Do not implement custom alias support partially; align DTO, service flow, uniqueness checks, and docs together.

## Validation Minimum
- Run relevant tests after code changes.
- For create flow changes: validate create endpoint behavior.
- For redirect flow changes: validate hit + miss behavior.
- For cache/persistence changes: explain what was and was not validated.
- For architecture-sensitive changes: note consistency/performance implications explicitly.
