# TASK_WORKFLOW.md

## Standard Task Flow

1. Understand the task and affected path
2. Identify whether it touches create flow, redirect flow, cache, or persistence
3. Read relevant docs:
   - `PROJECT_OVERVIEW.md`
   - `ARCHITECTURE.md`
   - `CONSTRAINTS.md`
4. Inspect affected classes before editing
5. Propose smallest viable change
6. Implement
7. Validate behavior
8. Update docs / `REPO_MEMORY.md` if a reusable lesson appears

## Before Editing
Ask:
- Is this change on the hot read path or write path?
- Is the component source-of-truth or optimization-only?
- Could this change affect consistency, latency, or collision safety?
- Is there any hidden async behavior here?

## During Editing
- Keep scope tight.
- Preserve controller -> service -> persistence/cache traceability.
- Avoid changing both architecture and API contract unless explicitly required.
- Prefer evidence from current code over assumptions from naming.

## Validation
### Minimum
- build/test relevant modules
- verify endpoint behavior if API path is touched
- verify cache/miss behavior if lookup path is touched

### Strongly Recommended
- test both existing alias and missing alias cases
- test create path with generated alias and optional expiration
- test startup/init-sensitive changes separately when possible

## When To Escalate / Ask Human
- consistency trade-off is unclear
- write-behind behavior may lose data
- alias uniqueness guarantee is uncertain
- DB/schema change is required
- performance optimization changes correctness semantics
