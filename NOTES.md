# NOTES

## Summary of changes

Every bug below was reproduced first (curl / browser), then re-checked after the fix.

1. **Search SQL operator precedence** (`TaskRepository`, `db/queries`, `db/oracle`): `AND` binds tighter than `OR`, so any description match skipped both `archived = FALSE` and the status filter. Archived tasks leaked into results and filters were ignored. Added parentheses.
2. **Artificial 0–1 s `Thread.sleep`** for short/blank queries in `TaskController`: removed (1.0 s → ~8 ms).
3. **Invalid `status` → 500**: now 400 with the allowed values.
4. **`page=0` / negative `pageSize` → 500** (`subList` out of bounds): clamped, `pageSize` capped at 100.
5. **In-memory pagination**: replaced with DB `LIMIT/OFFSET` + `COUNT` via `Pageable`; `id` tie-breaker for stable pages.
6. **LIKE wildcards** `%`/`_` not escaped: `q=%` matched everything.
7. **Frontend `useTasks`**: errors left `loading=true` forever (error never shown) and were never cleared. Out-of-order responses could overwrite newer results, now handled with `AbortController`.
8. **No debounce**: one request per keystroke; added a 300 ms debounce.
9. **Page not reset** on search/filter change: could strand the user on an empty page 4.
10. Minor: `System.out` → SLF4J, removed `console.log`, backend error message surfaced in UI.

## Assumptions

- Archived tasks should never appear in search.
- Unknown status is a client error (400), not "ignore the filter".
- Out-of-range paging is clamped rather than rejected.

## Not changed

- No auth / write endpoints. That's out of scope for a patch.
- Wildcard `LIKE '%term%'` can't use indexes. That's fine at this size; full-text search later.
- Did not add a test suite (timebox); verification was scripted curl + Playwright.

## Biggest remaining risk

No automated tests: the precedence bug existed in three copies of the same query and nothing caught it. Duplicated query logic across Java and PL/SQL will drift again.

## Tools / AI used

Claude Code: explored the code, reproduced each bug, drafted the fixes, ran the checks. I reviewed every change and wrote the handwritten notes myself.
