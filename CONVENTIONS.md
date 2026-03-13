# CONVENTIONS.md

## Design Conventions
- Keep controllers thin; business / orchestration logic belongs in services.
- Treat MySQL as durable source of truth.
- Treat Bloom filter and caches as optimization layers only.
- Keep key generation replaceable through `KeyGenerationService`.
- Avoid mixing infrastructure tuning changes with functional API behavior changes in one edit unless necessary.

## Package Conventions
- `controller` -> HTTP endpoints only
- `service` -> orchestration and use-case logic
- `service/impl` -> concrete service implementations
- `repository` -> JPA access
- `entity` -> persisted data model
- `dto` -> API data contracts
- `config` -> configuration
- `cache` / Redis / bloom-related logic -> performance support layers
- `utils` -> local helpers; do not hide core business logic here

## Coding Conventions
- Prefer explicit names for performance-sensitive paths.
- Preserve separation between lookup flow and persistence flow.
- Avoid hidden cross-layer coupling.
- Keep alias lookup semantics easy to trace from controller -> service -> cache/DB.

## Testing Conventions
- For redirect path changes, verify both hit and miss behavior.
- For create path changes, verify generated alias shape, response contract, and persistence behavior.
- Add regression tests for bugs around cache miss, duplicate alias, or delayed persistence.
- If changing performance-related logic, describe the expected impact explicitly even if no benchmark is added.

## API / Contract Conventions
- `POST /app/api/create` returns structured response with alias + shortened URL.
- `GET /{alias}` is redirect-oriented and must preserve lookup correctness.
- Any API shape change should be called out explicitly.

## Observability Conventions
- Performance-sensitive changes should preserve or improve traceability.
- Prefer logs/metrics that help explain cache hit/miss and failure mode when relevant.
