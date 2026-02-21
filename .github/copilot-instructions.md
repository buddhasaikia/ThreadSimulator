# Copilot Instructions for ThreadSimulator

## Project Overview

**ThreadSimulator** is an Android application that simulates and tests lazy list updates from multiple threads using Jetpack Compose. It demonstrates real-time multi-threaded data collection, metric aggregation, and adaptive UI rendering patterns with Kotlin Coroutines.

### Tech Stack
- **Language**: Kotlin
- **Target SDK**: 35 (Android 15)
- **Min SDK**: 24 (Android 7.0)
- **UI Framework**: Jetpack Compose
- **Dependency Injection**: Hilt/Dagger
- **Build System**: Gradle with Kotlin DSL
- **Async Runtime**: Kotlin Coroutines
- **Logging**: Timber

## Build, Test, and Lint Commands

### Build Commands
```bash
# Build the entire project
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Build release APK (unsigned)
./gradlew assembleRelease

# Build with clean
./gradlew clean build
```

### Testing Commands
```bash
# Run all unit tests
./gradlew test

# Run a specific test class
./gradlew test --tests "com.bs.threadsimulator.path.TestClassName"

# Run all instrumented (Android) tests
./gradlew connectedAndroidTest

# Run a specific instrumented test
./gradlew connectedAndroidTest --tests "com.bs.threadsimulator.path.TestClassName"
```

### Code Quality
```bash
# Run Android Lint (runs as part of build)
./gradlew lint

# Run lint and apply safe fixes
./gradlew lintFix

# Run all checks
./gradlew check
```

**Note**: While no external linters (ktlint, detekt) are currently integrated, Android Lint runs automatically during build.

### Running Locally
```bash
# Install debug APK to connected device/emulator
./gradlew installDebug

# Install and run in one command
./gradlew installDebug && adb shell am start -n com.bs.threadsimulator/.MainActivity
```

## Architecture Overview

### Layered Architecture
The project follows **Clean Architecture** with clear layer separation:

```
┌─────────────────────────────────────┐
│           UI Layer (Compose)        │
│  HomeScreen, Components, Theme      │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      ViewModel & State Management    │
│    HomeViewModel + Channels         │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│        Domain Layer (Use Cases)      │
│  FetchStock*UseCase, Init..UseCase  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│        Data Layer (Repository)       │
│  DataRepository + MockDataSource    │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│        Models & Common Utils         │
│  Resource, Company, ThreadMonitor   │
└─────────────────────────────────────┘
```

### Key Components

#### 1. **Data Layer** (`data/`)
- **DataRepository**: Orchestrates multi-threaded stock data fetching via channels
  - Manages thread lifecycle and channel buffering
  - Implements throttling strategies based on list size
  - Exposes Flow-based APIs: `fetchStockCurrentPrice()`, `fetchStockHighLow()`, `fetchStockPE()`
  - Uses `Channel<Resource<T>>` with `BufferOverflow.DROP_OLDEST` strategy (15000 capacity)
- **MockDataSource**: Simulates realistic stock data with delays
- **CompanyList**: Manages mock company data for testing

#### 2. **Domain Layer** (`domain/`)
- **Use Cases**: Thin wrapper layer coordinating data operations
  - `FetchStockCurrentPriceUseCase`, `FetchStockHighLowUseCase`, `FetchStockPEUseCase`
  - `InitCompanyListUseCase`, `SetUpdateIntervalUseCase`
  - Pattern: accepts Flow as input, returns Flow as output

#### 3. **UI Layer** (`ui/`)
- **HomeScreen**: Main composable managing state and list rendering
- **HomeViewModel**: 
  - Manages channel collection lifecycle
  - Aggregates metrics via `ThreadMonitor`
  - Applies throttling based on list size (`ThrottleStrategy`)
  - Uses `mutableStateListOf<Company>` for reactive list updates
- **Components**: `CompanyItem`, `IntervalInput`, theming

#### 4. **Common Utils** (`common/`)
- **ThreadMonitor**: Tracks thread-level metrics (ID, name, updates, timing)
- **ThrottleStrategy**: Configures throttling behavior (IMMEDIATE, THROTTLED, AGGRESSIVE)
- **AppDispatchers**: Centralized dispatcher configuration
- **FlowExt**: Flow extension functions for throttling

#### 5. **Dependency Injection** (`di/`)
- **AppModule**: Hilt module providing singleton `ThreadMonitor`

### Threading Model
- **Multi-threaded Data Fetch**: Each use case spawns multiple coroutine jobs on `Dispatchers.IO`
- **Channel-Based Communication**: Jobs write `Resource<T>` objects to shared channel
- **Main Thread Rendering**: Channel receiver collects on `Dispatchers.Main` for UI updates
- **Throttling**: Applied adaptively based on metrics and list size

## Key Conventions

### File Organization
- **Packages follow domain first**: `data/`, `domain/`, `ui/`, `common/`
- **UI components**: Composables in `ui/screens/components/`
- **Models**: Data classes in `model/` directory
- **Use Cases**: Each in separate file, named `<Entity><Action>UseCase.kt`

### Naming Conventions
- **ViewModel suffix**: `HomeViewModel`
- **UseCase suffix**: `FetchStockHighLowUseCase`
- **Strategy pattern classes**: `ThrottleStrategy`
- **Composables**: CamelCase starting with capital letter
- **Private methods/properties**: `_` prefix for backing properties, `private fun` for helper functions

### State Management
- **Mutable State**: Use `mutableStateOf()`, `mutableStateListOf()` for Compose integration
- **Flows**: For async operations and data streams (preferred for ViewModel APIs)
- **Channels**: For multi-producer, single-consumer patterns (used in data layer)

### Coroutine Patterns
- **ViewModelScope**: All VM launches use `viewModelScope` (auto-cancellation)
- **Job Management**: Store jobs in `_activeJobs` list for manual cancellation on stop
- **Error Handling**: Use `try-catch` in VM, emit `Resource.Error` in repository

### Resource Wrapper Pattern
```kotlin
sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val exception: Exception) : Resource<T>()
    data class Loading<T> : Resource<T>()
}
```

### Dependency Injection
- **Hilt annotations**: `@HiltViewModel`, `@Module`, `@Provides`, `@Singleton`
- **Constructor injection**: In VMs and Repository
- **Qualifier custom injections**: Not currently used; add when needed

### Channel Configuration
- **Capacity**: 15,000 (configured in `DataRepository`)
- **Overflow Strategy**: `BufferOverflow.DROP_OLDEST` (discards oldest when full)
- **Use case**: Handles burst updates from multiple threads without blocking senders

### Logging
- **Framework**: Timber (configured in `App.kt`)
- **Usage**: `Timber.d()`, `Timber.i()`, `Timber.e()`
- **Setup**: BuildConfig generation enabled (see `app/build.gradle.kts`)
- **Note**: Debug logging can be controlled by setting `BuildConfig.DEBUG` appropriately

### Error Handling Strategy
- **Data layer**: Return `Resource.Error(exception)` wrapped in Timber logging
- **UI layer**: Display in dedicated scrollable error UI with up to 3 lines visible
- **Pattern**: Errors are caught and logged using Timber, then surfaced to user through `StateError` model
- **Retry**: Error state is dismissible; can retry by adjusting intervals or restarting fetch operations

## Known Limitations & Future Work

### Current Gaps (from IMPROVEMENTS.md)
1. **Test Coverage**: Only placeholder tests exist; comprehensive unit and instrumented tests needed
2. **Performance**: `mutableStateListOf` causes full list recomposition; needs key-based optimization
3. **Configuration**: Channel size and throttle strategies are hardcoded; should be configurable
4. **Metrics Export**: Advanced thread metrics tracking and export (CSV/JSON) not yet implemented

### Quick Wins for Contributors
- Enable Compose compiler strong skipping mode (currently commented in app/build.gradle.kts)
- Implement comprehensive test suite (unit + instrumented) - currently only placeholders
- Add input validation and edge-case handling to `IntervalInput` component
- Improve adaptive throttling based on device performance metrics

### Performance Considerations
- `mutableStateListOf` causes full recomposition on any change; optimize with key-based updates
- Channel with DROP_OLDEST strategy discards updates under extreme load; monitor dropped items
- Throttling is static; adaptive throttling based on device performance would improve responsiveness

## Hilt Configuration
- **Scopes**: Currently using `@Singleton` only
- **Modules**: Single `AppModule`; expand to feature modules when multi-screen architecture added
- **EntryPoint**: Used in `App.kt` for initialization
- **Qualifiers**: None currently; use `@Named`, `@Qualifier` when multiple implementations needed

## Important Files Quick Reference
- **Build config**: `app/build.gradle.kts`, `settings.gradle.kts`
- **Main entry**: `App.kt`, `MainActivity.kt`
- **Core logic**: `HomeViewModel.kt`, `DataRepository.kt`
- **Metrics tracking**: `ThreadMonitor.kt` (in common/)
- **Theme/Styling**: `ui/theme/` (Color.kt, Theme.kt, Type.kt)
- **Error handling**: `StateError.kt` (in model/)
- **Roadmap**: `IMPROVEMENTS.md` (comprehensive improvement plan)

## Common Workflows

### Adding a New Feature
1. Create use case in `domain/` following `<Entity><Action>UseCase.kt` pattern
2. Add repository method to `DataRepository` with appropriate KDoc
3. Inject use case in `HomeViewModel` via Hilt
4. Manage state via `mutableStateOf()` or collect flows on `viewModelScope`
5. Create UI component in `ui/screens/components/` as a Composable
6. Log important operations using `Timber.d()` or `Timber.i()`

### Modifying Threading Behavior
1. Update constants in `DataRepository.kt` (Constants object)
2. Adjust `ThrottleStrategy` enum if new behavior is needed
3. Apply throttling via `FlowExt.throttle()` extension in channel collection
4. Record metrics through `ThreadMonitor.recordMetrics()` calls
5. Test with multiple update intervals to verify behavior

### Debugging Multi-threaded Issues
1. Enable Timber logging with `Timber.d()` in suspect areas
2. Use `ThreadMonitor.getMetrics()` to inspect thread-level statistics
3. Check `HomeViewModel` for active jobs and ensure proper cancellation
4. Monitor channel buffer state via emitted metrics
5. Use Android Studio's profiler to track memory and thread creation

## Next Steps for New Contributors
1. Read `IMPROVEMENTS.md` for context on planned enhancements
2. Check `HomeViewModel` and data layer for inline TODOs and KDoc
3. Start with test coverage—it has the most immediate impact
4. When adding features, follow the domain-layered package structure
5. Use Timber for all new logging; avoid direct `Log` calls

## MCP Servers & IDE Integration

For enhanced Copilot CLI integration with Android development tools, see `.github/mcp-servers.md`:
- **GitHub MCP Server** (built-in): PR/issue management and workflow status
- **Gradle MCP Server** (optional): Build and test automation
- **ADB MCP Server** (optional): Device and emulator management

Example template config: `.github/mcp-config.json.example`
