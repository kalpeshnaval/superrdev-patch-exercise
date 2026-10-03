# NOTES

## Summary of changes

Each bug was reproduced (curl/browser) before fixing and re-checked after.

1. **Search SQL precedence** (repository + both `db/` files): `AND` binds tighter than `OR`, so description matches bypassed `archived = FALSE` and the status filter. Added parentheses.
2. **Artificial `Thread.sleep`** (up to 1 s on short queries): removed.
3. **Invalid `status` → 500**: now 400.
4. **`page=0` / negative `pageSize` → 500**: clamped; `pageSize` capped at 100.
5. **In-memory pagination** → DB paging (`Pageable` + count), `id` tie-breaker.
6. **Unescaped LIKE wildcards**: `q=%` matched everything.
7. **`useTasks`**: errors left "Loading…" forever and never cleared; stale responses could overwrite newer ones (`AbortController`).
8. **No debounce / page not reset** on new search or filter.
9. `archived` made `NOT NULL` (primitive `boolean` in entity).

**Improvements:** 7 backend integration tests (`./mvnw test`), which fail against the original query. UI: rows stay visible while loading, result count, readable statuses, Created column, clear button, labels, mobile layout. `npm audit fix` (7 → 2 vulnerabilities).

## Assumptions

- Archived tasks never appear in search.
- Unknown status is a client error; out-of-range paging is clamped.

## Not changed

- Vite 5 → 8 major upgrade (remaining 2 advisories are dev-server only).
- `LIKE '%term%'` can't use indexes; fine at this size.
- H2 console and `show-sql` left on (dev setup).

## Biggest remaining risk

Search logic is duplicated across Java and PL/SQL with no shared tests, so they will drift. No auth or frontend tests.

## Tools / AI used

Claude Code explored the code, reproduced bugs, drafted fixes and tests, and ran curl/Playwright checks. I reviewed every change.
