### Title:
ThrottleStrategy constants are Long but typed as object

### Description:
## Issue Description

### Current State
`ThrottleStrategy` is declared as a Kotlin `object` containing `const val Long` constants:

```kotlin
object ThrottleStrategy {
    const val RAPID = 16L
    const val NORMAL = 32L
    const val RELAXED = 64L
}
```

Because the constants are plain `Long` values, all call-sites that store or accept a throttle strategy (e.g. `currentThrottleStrategy`, `setUpdateThrottling(strategy: Long)`) are typed as `Long`. This means:

1. **No type safety** — any arbitrary `Long` (e.g. `-1L`, `999999L`) is accepted without compile-time error, making it easy to introduce subtle bugs.
2. **Poor discoverability** — consumers have to know to look inside the `ThrottleStrategy` object for valid values; the `Long` parameter type gives no hint.
3. **Misleading design** — using `object` creates the appearance of a namespace or singleton pattern, but it's really just a bag of constants — a job better suited to an `enum class` or `@JvmInline value class`.

### Proposed Fix
Refactor `ThrottleStrategy` to a sealed class or enum class:

```kotlin
enum class ThrottleStrategy(val intervalMs: Long) {
    RAPID(16L),
    NORMAL(32L),
    RELAXED(64L);

    companion object {
        fun forUpdateType(updateType: String): ThrottleStrategy =
            when (updateType) {
                "current_price" -> RAPID
                "high_low" -> NORMAL
                "PE" -> RELAXED
                else -> NORMAL
            }
    }
}
```

Then update consuming code to use `ThrottleStrategy` as the parameter/variable type instead of `Long`:

```kotlin
// Before
private var currentThrottleStrategy = ThrottleStrategy.NORMAL   // inferred as Long
private fun setUpdateThrottling(strategy: Long) { ... }

// After
private var currentThrottleStrategy = ThrottleStrategy.NORMAL   // inferred as ThrottleStrategy
private fun setUpdateThrottling(strategy: ThrottleStrategy) { ... }
```

### Affected Files
- `app/src/main/java/com/bs/threadsimulator/common/ThrottleStrategy.kt` — refactor from `object` to `enum class`
- `app/src/main/java/com/bs/threadsimulator/ui/screens/HomeViewModel.kt` — update `currentThrottleStrategy` type, `setUpdateThrottling` parameter, and `throttleUpdates` call to use `strategy.intervalMs`

### Priority
LOW - Code Quality / Type Safety

### Acceptance Criteria
- [ ] `ThrottleStrategy` refactored to `enum class` (or sealed/inline value class)
- [ ] `currentThrottleStrategy` and `setUpdateThrottling` use `ThrottleStrategy` type instead of `Long`
- [ ] Arbitrary `Long` values can no longer be passed where a throttle strategy is expected
- [ ] `forUpdateType()` function migrated to the new type
- [ ] All existing tests pass
