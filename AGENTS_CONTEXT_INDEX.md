# AGENTS_CONTEXT_INDEX.md

## Read Order for Coding Agents
1. `PROJECT_OVERVIEW.md`
2. `ARCHITECTURE.md`
3. `CONSTRAINTS.md`
4. `CONVENTIONS.md`
5. `TASK_WORKFLOW.md`
6. `MODULE_MAP.md`
7. `REPO_MEMORY.md`
8. `ARCHITECTURE_REVIEW.md`
9. `KNOWN_GAPS.md`
10. `REFACTOR_ROADMAP.md`

## Quick Orientation
If task touches:
- API contract -> read overview + conventions + constraints
- redirect / lookup -> read architecture + constraints + repo memory
- create / persistence -> read architecture + constraints + repo memory
- performance tuning -> read architecture + constraints + module map

## Principle
Do not assume caches or bloom filter are source of truth.
Always identify whether the touched component is:
- authoritative
- optimization-only
- async/buffered

Also verify whether documented/API-declared behavior is truly implemented in the current primary flow.
A known example in this repo is custom alias support.
