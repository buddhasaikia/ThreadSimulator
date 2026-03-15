# ThreadSimulator exportMetrics - Implementation Template

This file contains ready-to-use code templates for completing the exportMetrics implementation.

---

## TEMPLATE 1: ExportMetricsUseCase (HIGH PRIORITY ⭐⭐⭐)

**File to create**: `feature/stockData/src/main/java/com/bs/threadsimulator/feature/stockdata/domain/ExportMetricsUseCase.kt`

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
 *
 * Metrics exported include:
 * - threadId: Unique thread identifier
 * - threadName: Human-readable thread name
 * - updateType: Type of update ("PE", "CurrentPrice", "HighLow")
 * - updateCount: Total number of updates
 * - avgUpdateTimeMs: Average time per update in milliseconds
 * - peakUpdatesPerSec: Highest updates-per-second rate observed
 * - stateTransitions: Count of thread state changes
 * - queueDepth: Current channel buffer occupancy
 * - threadAllocatedBytes: Process-level memory allocation
 * - jitterMs: Standard deviation of update intervals
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
         * Retrieves a snapshot of current metrics from ThreadMonitor, converts them to
         * a map representation, and delegates to MetricsExporter for CSV formatting.
         *
         * @return ExportedMetrics.Success with file path and format, or ExportedMetrics.Error
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
         * Retrieves a snapshot of current metrics from ThreadMonitor, converts them to
         * a map representation, and delegates to MetricsExporter for JSON formatting.
         *
         * @return ExportedMetrics.Success with file path and format, or ExportedMetrics.Error
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

## TEMPLATE 2: HomeViewModel - Updated Constructor & StateFlow

**Location**: `feature/stockData/src/main/java/com/bs/threadsimulator/feature/stockdata/ui/screens/HomeViewModel.kt`

### Changes to constructor (add these imports):
```kotlin
import com.bs.threadsimulator.model.MetricsExporter
import com.bs.threadsimulator.feature.stockdata.domain.ExportMetricsUseCase
```

### Updated constructor parameter (add to @Inject constructor):
```kotlin
@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val fetchStockCurrentPriceUseCase: FetchStockCurrentPriceUseCase,
        private val fetchStockHighLowUseCase: FetchStockHighLowUseCase,
        private val fetchStockPEUseCase: FetchStockPEUseCase,
        private val setUpdateIntervalUseCase: SetUpdateIntervalUseCase,
        private val initCompanyListUseCase: InitCompanyListUseCase,
        private val getCompanyListUseCase: GetCompanyListUseCase,
        private val streamCoordinationService: StreamCoordinationService,
        // ADD THESE:
        private val exportMetricsUseCase: ExportMetricsUseCase,
        private val metricsExporter: MetricsExporter,  // Optional, for direct access if needed
    ) : ViewModel() {
```

### Add StateFlow for export result (add after errorMessage):
```kotlin
        private val _exportResult = MutableStateFlow<ExportedMetrics?>(null)
        
        /**
         * Observable export operation result.
         *
         * Emits ExportedMetrics.Success or ExportedMetrics.Error when export completes.
         * Consumers can observe this to display success/error messages to the user.
         */
        val exportResult: StateFlow<ExportedMetrics?> = _exportResult.asStateFlow()
```

### Add import for ExportedMetrics:
```kotlin
import com.bs.threadsimulator.model.ExportedMetrics
```

---

## TEMPLATE 3: HomeViewModel - Replace exportMetrics() Implementation

**Replace lines 361-364 with:**

```kotlin
        /**
         * Exports collected metrics to the specified format.
         *
         * Launches a coroutine on the IO dispatcher to perform the export without blocking.
         * Updates [exportResult] StateFlow with the result (Success or Error).
         *
         * @param format Export format: "csv" or "json"
         */
        private fun exportMetrics(format: String) {
            viewModelScope.launch(streamCoordinationService.dispatchers.ioDispatcher) {
                val result = when (format.lowercase()) {
                    "csv" -> exportMetricsUseCase.executeCSV()
                    "json" -> exportMetricsUseCase.executeJSON()
                    else -> ExportedMetrics.Error("Unknown export format: $format")
                }
                
                _exportResult.value = result
                
                when (result) {
                    is ExportedMetrics.Success -> {
                        Timber.i(
                            "Metrics exported successfully: %s (%s)",
                            result.filePath,
                            result.format,
                        )
                        errorMessage.value = null
                    }
                    is ExportedMetrics.Error -> {
                        Timber.e("Export failed: %s", result.message)
                        errorMessage.value = result.message
                    }
                }
            }
        }
```

---

## TEMPLATE 4: UI Integration - HomeScreenRoute (Optional)

**Location**: Wherever HomeScreenRoute observes export results

```kotlin
// In your Compose UI, observe the exportResult StateFlow:

val exportResult by homeViewModel.exportResult.collectAsState()

LaunchedEffect(exportResult) {
    exportResult?.let { result ->
        when (result) {
            is ExportedMetrics.Success -> {
                // Show success Snackbar or Toast
                snackbarHostState.showSnackbar(
                    message = "Metrics exported to ${result.fileName}",
                    duration = SnackbarDuration.Short,
                )
            }
            is ExportedMetrics.Error -> {
                // Show error Snackbar or Toast
                snackbarHostState.showSnackbar(
                    message = "Export failed: ${result.message}",
                    duration = SnackbarDuration.Long,
                )
            }
        }
    }
}
```

---

## TEMPLATE 5: Testing (Optional)

**File to create**: `feature/stockData/src/test/java/com/bs/threadsimulator/feature/stockdata/domain/ExportMetricsUseCaseTest.kt`

```kotlin
package com.bs.threadsimulator.feature.stockdata.domain

import com.bs.threadsimulator.common.ThreadMetrics
import com.bs.threadsimulator.common.ThreadMonitor
import com.bs.threadsimulator.model.ExportedMetrics
import com.bs.threadsimulator.model.MetricsExporter
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertIs

class ExportMetricsUseCaseTest {
    private lateinit var metricsExporter: MetricsExporter
    private lateinit var threadMonitor: ThreadMonitor
    private lateinit var useCase: ExportMetricsUseCase

    @Before
    fun setUp() {
        metricsExporter = mockk()
        threadMonitor = mockk()
        useCase = ExportMetricsUseCase(metricsExporter, threadMonitor)
    }

    @Test
    fun `executeCSV returns error when no metrics available`() = runTest {
        coEvery { threadMonitor.getMetricsSnapshot() } returns emptyList()

        val result = useCase.executeCSV()

        assertIs<ExportedMetrics.Error>(result)
    }

    @Test
    fun `executeCSV delegates to metricsExporter when metrics available`() = runTest {
        val mockMetric = ThreadMetrics(
            threadId = 1L,
            threadName = "TestThread",
            updateType = "TEST",
            updateCount = 100L,
            avgUpdateTimeMs = 50L,
        )
        coEvery { threadMonitor.getMetricsSnapshot() } returns listOf(mockMetric)
        coEvery { metricsExporter.exportToCSV(any()) } returns
            ExportedMetrics.Success(
                fileName = "test.csv",
                filePath = "/tmp/test.csv",
                format = "csv",
            )

        val result = useCase.executeCSV()

        assertIs<ExportedMetrics.Success>(result)
    }
}
```

---

## Checklist for Implementation

- [ ] Create `ExportMetricsUseCase.kt` (Template 1)
- [ ] Update HomeViewModel constructor with new dependencies (Template 2)
- [ ] Add StateFlow for export result (Template 2)
- [ ] Replace `exportMetrics()` implementation (Template 3)
- [ ] Update UI to observe `exportResult` (Template 4)
- [ ] Create unit tests (Template 5 - optional)
- [ ] Test CSV export manually
- [ ] Test JSON export manually
- [ ] Test error scenarios (no metrics available, export failure)
- [ ] Verify files are created in cache directory
- [ ] Check that UI properly displays success/error messages

---

## Files Modified/Created

| File | Action | Status |
|------|--------|--------|
| ExportMetricsUseCase.kt | Create | ❌ |
| HomeViewModel.kt | Update | ❌ |
| HomeScreenRoute.kt (optional) | Update | ⏳ |
| ExportMetricsUseCaseTest.kt (optional) | Create | ⏳ |

---

## Expected Output Files

When implemented correctly, users will see:

**CSV Export**:
- File: `metrics_20240115_143022.csv`
- Location: `/data/data/com.bs.threadsimulator/cache/metrics_20240115_143022.csv`
- Content: Header row + data rows with all 10 metrics

**JSON Export**:
- File: `metrics_20240115_143022.json`
- Location: `/data/data/com.bs.threadsimulator/cache/metrics_20240115_143022.json`
- Content: JSON array of objects with all 10 metrics

