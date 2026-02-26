### Title:
`updateFromDomain` creates new `Stock` on every update

### Description:
## Issue Description
From IMPROVEMENTS.md section 2.3 / 8.1 - UI Performance & HomeViewModel Issues

### Current State
In `Company.updateFromDomain()`, a brand-new `Stock` object is instantiated on every call:

```kotlin
fun updateFromDomain(data: CompanyData) {
    this.threadName = data.threadName
    this.peRatio = data.peRatio
    this.stock =
        Stock(
            data.stock.symbol,
            data.stock.openingPrice,
            data.stock.closingPrice,
            data.stock.low,
            data.stock.high,
            data.stock.currentPrice,
        )
}
```

The `Stock` class already exposes mutable Compose state properties (`low`, `high`, `currentPrice`) that can be updated in place. Creating a new `Stock` instance every time:

1. **Allocates unnecessary objects** — a new `Stock` is created on every price update for every company, generating significant GC pressure during high-frequency updates.
2. **Triggers broader recompositions** — assigning a new object to `Company.stock` (a `mutableStateOf` delegate) invalidates every composable reading that property, even when only `currentPrice` changed.
3. **Discards existing mutable state** — the in-place `mutableStateOf` delegates inside `Stock` (`low`, `high`, `currentPrice`) are never leveraged; their purpose is defeated by wholesale replacement.

### Proposed Fix
Update `Company.updateFromDomain()` to mutate the existing `Stock`'s mutable fields instead of replacing the entire object:

```kotlin
fun updateFromDomain(data: CompanyData) {
    this.threadName = data.threadName
    this.peRatio = data.peRatio
    this.stock.low = data.stock.low
    this.stock.high = data.stock.high
    this.stock.currentPrice = data.stock.currentPrice
}
```

If `symbol`, `openingPrice`, or `closingPrice` also need updating, consider making them `var` with `mutableStateOf` delegates on `Stock` as well. Otherwise, only replace the entire `Stock` when those immutable fields actually change.

### Affected Files
- `app/src/main/java/com/bs/threadsimulator/model/Company.kt` — `updateFromDomain()`
- `app/src/main/java/com/bs/threadsimulator/model/Stock.kt` — may need additional mutable fields

### Priority
MEDIUM - Performance

### Acceptance Criteria
- [ ] `updateFromDomain` updates existing `Stock` fields in place instead of creating a new instance
- [ ] No unnecessary object allocations per update cycle
- [ ] Compose recompositions are scoped to only the fields that changed
- [ ] Existing unit tests continue to pass
- [ ] Thread simulation performance is not degraded
