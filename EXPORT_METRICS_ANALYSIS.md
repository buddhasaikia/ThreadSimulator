# ThreadSimulator exportMetrics Infrastructure Analysis

## Executive Summary
The ThreadSimulator project has **partial** exportMetrics infrastructure in place:
- ✅ **Interfaces defined**: MetricsExporter interface & ExportedMetrics sealed class
- ✅ **Implementation complete**: MetricsExporterImpl with CSV/JSON support
- ✅ **ThreadMonitor metrics ready**: Rich thread execution data available
- ❌ **UseCase missing**: No ExportMetricsUseCase created yet
- ❌ **Feature integration incomplete**: HomeViewModel placeholder only

---

## Quick File Reference

| File | Component | Status |
|------|-----------|--------|
| `core/model/ExportedMetrics.kt` | Result sealed class | ✅ Complete |
| `core/model/MetricsExporter.kt` | Interface definition | ✅ Complete |
| `app/common/MetricsExporterImpl.kt` | Implementation (CSV/JSON) | ✅ Complete |
| `core/common/ThreadMetrics.kt` | Metrics data class & ThreadMonitor | ✅ Complete |
| `feature/stockData/ui/screens/HomeViewModel.kt` | ViewModel placeholder | ❌ Incomplete |
| `feature/stockData/domain/ExportMetricsUseCase.kt` | **MISSING - TO CREATE** | ❌ Missing |
| `feature/stockData/di/FeatureModule.kt` | DI configuration | ✅ Ready |
| `app/di/AppModule.kt` | MetricsExporter DI | ✅ Ready |
| `core/di/CoreModule.kt` | ThreadMonitor DI | ✅ Ready |

---

## 1. CURRENT HOMEVIEWMODEL IMPLEMENTATION

**File**: `feature/stockData/src/main/java/com/bs/threadsimulator/feature/stockdata/ui/screens/HomeViewModel.kt`

**Lines 354-382**: Currently a placeholder

```kotlin
private fun exportMetrics(format: String) {
    Timber.d("Export metrics ($format) - NOT YET IMPLEMENTED in feature module")
    errorMessage.value = "Export not yet available in this module"
}

fun exportMetricsCSV() {
    exportMetrics("csv")
}

fun exportMetricsJSON() {
    exportMetrics("json")
}
```

**Available Data**:
- `threadMetrics: StateFlow<List<ThreadMetrics>>` (via `streamCoordinationService.monitor.metrics`)
- `streamCoordinationService: StreamCoordinationService` (already injected)

---

## 2. METRICSEXPORTER INTERFACE

**File**: `core/model/MetricsExporter.kt`

```kotlin
interface MetricsExporter {
    suspend fun exportToCSV(metrics: List<Map<String, Any>>): ExportedMetrics
    suspend fun exportToJSON(metrics: List<Map<String, Any>>): ExportedMetrics
}
```

Both methods:
- Accept serialized metrics as `List<Map<String, Any>>`
- Return `ExportedMetrics` sealed class (Success or Error)
- Are suspend functions (safe for coroutines)

---

## 3. EXPORTEDMETRICS SEALED CLASS

**File**: `core/model/ExportedMetrics.kt`

```kotlin
sealed class ExportedMetrics {
    data class Success(
        val fileName: String,
        val filePath: String,
        val format: String,
    ) : ExportedMetrics()

    data class Error(
        val message: String,
    ) : ExportedMetrics()
}
```

**Two states**:
- **Success**: fileName, filePath, format (csv/json)
- **Error**: error message only

---

## 4. EXPORTMETRICSUSECAS - MISSING ❌

**Status**: Does not exist

**Should be created at**: 
`feature/stockData/src/main/java/com/bs/threadsimulator/feature/stockdata/domain/ExportMetricsUseCase.kt`

**Template** (based on architecture pattern):
```kotlin
class ExportMetricsUseCase
    @Inject
    constructor(
        private val metricsExporter: MetricsExporter,
        private val threadMonitor: ThreadMonitor,
    ) {
        suspend fun executeCSV(): ExportedMetrics {
            val snapshot = threadMonitor.getMetricsSnapshot()
            val metricsMap = snapshot.map { metric ->
                mapOf(
                    "threadId" to metric.threadId,
                    "threadName" to metric.threadName,
                    "updateType" to metric.updateType,
                    "updateCount" to metric.updateCount,
                    "avgUpdateTimeMs" to metric.avgUpdateTimeMs,
                    "peakUpdatesPerSec" to metric.peakUpdatesPerSec,
                    "stateTransitions" to metric.stateTransitions,
                    "queueDepth" to metric.queueDepth,
                    "threadAllocatedBytes" to metric.threadAllocatedBytes,
                    "jitterMs" to metric.jitterMs,
                )
            }
            return metricsExporter.exportToCSV(metricsMap)
        }

        suspend fun executeJSON(): ExportedMetrics {
            val snapshot = threadMonitor.getMetricsSnapshot()
            val metricsMap = snapshot.map { /* same as above */ }
            return metricsExporter.exportToJSON(metricsMap)
        }
    }
```

---

## 5. THREADMONITOR - AVAILABLE METRICS

**File**: `core/common/ThreadMetrics.kt`

### ThreadMetrics Data Class
```kotlin
data class ThreadMetrics(
    val threadId: Long,
    val threadName: String,
    val updateType: String,
    val updateCount: Long,
    val avgUpdateTimeMs: Long,
    val peakUpdatesPerSec: Double = 0.0,
    val stateTransitions: Int = 0,
    val queueDepth: Int = 0,
    val threadAllocatedBytes: Long = -1L,
    val jitterMs: Double = 0.0,
)
```

### 10 Available Metrics

| Metric | Type | Description | Example |
|--------|------|-------------|---------|
| threadId | Long | Thread identifier | 42 |
| threadName | String | Thread name | "DefaultDispatcher-worker-1" |
| updateType | String | Update type | "PE" or "CurrentPrice" or "HighLow" |
| updateCount | Long | Total updates | 1500 |
| avgUpdateTimeMs | Long | Average time per update | 45 |
| peakUpdatesPerSec | Double | Peak throughput | 22.5 |
| stateTransitions | Int | Thread state changes | 87 |
| queueDepth | Int | Channel buffer occupancy | 1250 |
| threadAllocatedBytes | Long | Process-level memory | 5242880 |
| jitterMs | Double | Interval std-dev | 12.3 |

### Accessing Metrics

```kotlin
// StateFlow for reactive collection
val metricsFlow = threadMonitor.metrics

// Snapshot for export
val snapshot = threadMonitor.getMetricsSnapshot()

// Convert to exportable format
val exportMap = snapshot.map { metric ->
    mapOf(
        "threadId" to metric.threadId,
        "threadName" to metric.threadName,
        "updateType" to metric.updateType,
        "updateCount" to metric.updateCount,
        "avgUpdateTimeMs" to metric.avgUpdateTimeMs,
        "peakUpdatesPerSec" to metric.peakUpdatesPerSec,
        "stateTransitions" to metric.stateTransitions,
        "queueDepth" to metric.queueDepth,
        "threadAllocatedBytes" to metric.threadAllocatedBytes,
        "jitterMs" to metric.jitterMs,
    )
}
```

---

## 6. EXISTING EXPORTERS & IMPLEMENTATIONS

### MetricsExporterImpl (COMPLETE)

**File**: `app/common/MetricsExporterImpl.kt`

#### CSV Export
```kotlin
override suspend fun exportToCSV(metrics: List<Map<String, Any>>): ExportedMetrics =
    try {
        val timestamp = timeFormat.format(Date())
        val fileName = "metrics_$timestamp.csv"
        val file = File(context.cacheDir, fileName)
        val csvContent = buildCSVContent(metrics)
        file.writeText(csvContent)
        ExportedMetrics.Success(
            filePath = file.absolutePath,
            fileName = fileName,
            format = "csv",
        )
    } catch (e: Exception) {
        ExportedMetrics.Error("CSV export failed: ${e.message}")
    }
```

#### JSON Export
```kotlin
override suspend fun exportToJSON(metrics: List<Map<String, Any>>): ExportedMetrics =
    try {
        val timestamp = timeFormat.format(Date())
        val fileName = "metrics_$timestamp.json"
        val file = File(context.cacheDir, fileName)
        val jsonContent = buildJSONContent(metrics)
        file.writeText(jsonContent)
        ExportedMetrics.Success(
            filePath = file.absolutePath,
            fileName = fileName,
            format = "json",
        )
    } catch (e: Exception) {
        ExportedMetrics.Error("JSON export failed: ${e.message}")
    }
```

#### Export Format Details
- **Location**: App cache directory
- **Naming**: `metrics_yyyyMMdd_HHmmss.{csv|json}`
- **Format**: 
  - CSV: Header + rows
  - JSON: Array of objects

---

## 7. ARCHITECTURE PATTERN FOR USE CASES

### Example 1: Simple UseCase (InitCompanyListUseCase)

```kotlin
class InitCompanyListUseCase
    @Inject
    constructor(
        private val stockRepository: StockRepository,
    ) {
        suspend fun execute(listSize: Int) {
            stockRepository.initCompanyList(listSize)
        }
    }
```

### Example 2: Complex UseCase (FetchStockCurrentPriceUseCase)

```kotlin
class FetchStockCurrentPriceUseCase
    @Inject
    constructor(
        private val stockRepository: StockRepository,
    ) {
        suspend fun execute(symbol: String): Flow<Resource<CompanyData>> =
            stockRepository
                .fetchStockCurrentPrice(symbol)
                .mapToDomainResource()
    }
```

### Pattern Characteristics
- Single responsibility
- Constructor injection
- Suspend function execute()
- Comprehensive KDoc
- Located in `feature/stockData/domain/`

---

## 8. DEPENDENCY INJECTION SETUP

### MetricsExporter is provided by AppModule

**File**: `app/di/AppModule.kt`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideMetricsExporter(
        @ApplicationContext context: Context,
    ): MetricsExporter = MetricsExporter(context)
}
```

### ThreadMonitor is provided by CoreModule

**File**: `core/di/CoreModule.kt`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    @Provides
    @Singleton
    fun provideThreadMonitor(): ThreadMonitor = ThreadMonitor()

    @Provides
    @Singleton
    fun provideAppDispatchers(): AppDispatchers = DefaultAppDispatchers()

    @Provides
    @Singleton
    fun provideChannelConfig(): ChannelConfig = ChannelConfig()
}
```

### Both are auto-available in Feature Module

No additional DI configuration needed. Dependencies are auto-injected via constructor injection.

---

## 9. IMPLEMENTATION ROADMAP

### Step 1: Create ExportMetricsUseCase ✅
- File: `feature/stockData/domain/ExportMetricsUseCase.kt`
- Dependencies: MetricsExporter, ThreadMonitor
- Methods: executeCSV(), executeJSON()

### Step 2: Update HomeViewModel ✅
- Inject ExportMetricsUseCase
- Inject MetricsExporter (for direct access if needed)
- Replace placeholder with actual implementation
- Launch coroutine in viewModelScope
- Update errorMessage or add exportResult StateFlow

### Step 3: Add Export Result State ✅
```kotlin
private val _exportResult = MutableStateFlow<ExportedMetrics?>(null)
val exportResult: StateFlow<ExportedMetrics?> = _exportResult.asStateFlow()
```

### Step 4: Update HomeScreenRoute (if needed) ✅
- Observe exportResult StateFlow
- Show success message with file path
- Show error message on failure

---

## 10. WHAT'S COMPLETE vs MISSING

### ✅ Complete & Ready
1. MetricsExporter interface - defined in core/model
2. ExportedMetrics sealed class - Success/Error states
3. MetricsExporterImpl - Full CSV/JSON implementation
4. ThreadMonitor - Collecting rich metrics
5. ThreadMetrics - 10 different metrics fields
6. CSV export - Implemented in MetricsExporterImpl
7. JSON export - Implemented in MetricsExporterImpl
8. DI setup - All dependencies provided
9. Architecture patterns - Established in codebase

### ❌ Still Needed
1. **ExportMetricsUseCase** - Missing (highest priority)
2. **HomeViewModel integration** - Currently placeholder
3. **Export status StateFlow** - For UI feedback
4. **Error UI handling** - Display export errors properly

---

## 11. QUICK COPY-PASTE: EXPORT METRICS USE CASE

Save to: `feature/stockData/src/main/java/com/bs/threadsimulator/feature/stockdata/domain/ExportMetricsUseCase.kt`

```kotlin
package com.bs.threadsimulator.feature.stockdata.domain

import com.bs.threadsimulator.common.ThreadMonitor
import com.bs.threadsimulator.model.ExportedMetrics
import com.bs.threadsimulator.model.MetricsExporter
import javax.inject.Inject

/**
 * Use case for exporting collected thread metrics.
 *
 * Encapsulates the business logic for exporting metrics snapshots to CSV or JSON formats.
 * Retrieves metrics from [ThreadMonitor] and delegates serialization to [MetricsExporter].
 */
class ExportMetricsUseCase
    @Inject
    constructor(
        private val metricsExporter: MetricsExporter,
        private val threadMonitor: ThreadMonitor,
    ) {
        /**
         * Exports collected metrics to CSV format.
         *
         * @return ExportedMetrics.Success with file path or ExportedMetrics.Error
         */
        suspend fun executeCSV(): ExportedMetrics {
            val snapshot = threadMonitor.getMetricsSnapshot()
            if (snapshot.isEmpty()) {
                return ExportedMetrics.Error("No metrics available to export")
            }
            val metricsMap = snapshot.map { metric ->
                mapOf(
                    "threadId" to metric.threadId,
                    "threadName" to metric.threadName,
                    "updateType" to metric.updateType,
                    "updateCount" to metric.updateCount,
                    "avgUpdateTimeMs" to metric.avgUpdateTimeMs,
                    "peakUpdatesPerSec" to metric.peakUpdatesPerSec,
                    "stateTransitions" to metric.stateTransitions,
                    "queueDepth" to metric.queueDepth,
                    "threadAllocatedBytes" to metric.threadAllocatedBytes,
                    "jitterMs" to metric.jitterMs,
                )
            }
            return metricsExporter.exportToCSV(metricsMap)
        }

        /**
         * Exports collected metrics to JSON format.
         *
         * @return ExportedMetrics.Success with file path or ExportedMetrics.Error
         */
        suspend fun executeJSON(): ExportedMetrics {
            val snapshot = threadMonitor.getMetricsSnapshot()
            if (snapshot.isEmpty()) {
                return ExportedMetrics.Error("No metrics available to export")
            }
            val metricsMap = snapshot.map { metric ->
                mapOf(
                    "threadId" to metric.threadId,
                    "threadName" to metric.threadName,
                    "updateType" to metric.updateType,
                    "updateCount" to metric.updateCount,
                    "avgUpdateTimeMs" to metric.avgUpdateTimeMs,
                    "peakUpdatesPerSec" to metric.peakUpdatesPerSec,
                    "stateTransitions" to metric.stateTransitions,
                    "queueDepth" to metric.queueDepth,
                    "threadAllocatedBytes" to metric.threadAllocatedBytes,
                    "jitterMs" to metric.jitterMs,
                )
            }
            return metricsExporter.exportToJSON(metricsMap)
        }
    }
```

---

## Summary Table

| Component | Status | Location |
|-----------|--------|----------|
| MetricsExporter Interface | ✅ | core/model/MetricsExporter.kt |
| ExportedMetrics Sealed Class | ✅ | core/model/ExportedMetrics.kt |
| MetricsExporterImpl | ✅ | app/common/MetricsExporterImpl.kt |
| ThreadMonitor | ✅ | core/common/ThreadMetrics.kt |
| ThreadMetrics Data (10 fields) | ✅ | core/common/ThreadMetrics.kt |
| CSV Export Format | ✅ | Implemented in MetricsExporterImpl |
| JSON Export Format | ✅ | Implemented in MetricsExporterImpl |
| **ExportMetricsUseCase** | ❌ | **feature/stockData/domain/** |
| HomeViewModel Integration | ❌ | feature/stockData/ui/screens/HomeViewModel.kt |
| Export Status StateFlow | ❌ | Need to add to HomeViewModel |

---

**Last Updated**: Analysis complete  
**Ready for Implementation**: YES - All dependencies are in place, only UseCase and ViewModel integration needed
