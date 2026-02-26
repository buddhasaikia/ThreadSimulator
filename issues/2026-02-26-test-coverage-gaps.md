### Title:
Test coverage gaps across domain, mapper, and UI layers

### Description:
## Issue Description
From IMPROVEMENTS.md sections 1.1 and 6.1 - Missing Proper Test Coverage & Test Framework Setup

### Current State

**Existing test coverage** (6 test files, 1 instrumented test):
- `HomeViewModelTest` — 10 tests (init, stop, setInterval, populateList, errorMessage)
- `DataRepositoryTest` — 8 tests (company list, interval setters, initialization)
- `MockDataSourceTest` — 10 tests (data generation, validation)
- `ThreadMonitorTest` — 11 tests (metrics recording, flow emissions)
- `MetricsExporterTest` — 10 tests (CSV/JSON export, edge cases)
- `CompanyItemTest` — 1 instrumented UI test

### Missing Coverage

#### Domain Layer (0% covered — 5 use cases, no tests)
- `FetchStockCurrentPriceUseCase`
- `FetchStockHighLowUseCase`
- `FetchStockPEUseCase`
- `SetUpdateIntervalUseCase`
- `InitCompanyListUseCase`
- `ExportMetricsUseCase`

#### Mapper Layer (0% covered — 3 mappers, no tests)
- `DataToDomainMapper`
- `DataToUIMapper`
- `DomainToUIMapper`

#### Repository Layer
- `StockRepository` — no unit tests (only indirectly tested via ViewModel)

#### Utility / Common Layer
- `FlowExt` (`throttleUpdates`) — no tests for throttling behaviour
- `ThrottleStrategy.forUpdateType()` — no tests
- `InputValidator` — no tests

#### UI Model Layer
- `Company.updateFromDomain()` — no tests
- `Stock` mutable state behaviour — no tests
- `Resource` sealed class — no tests

#### UI / Compose Layer
- `HomeScreenRoute` — no instrumented tests
- `IntervalInput` — no instrumented tests
- Only 1 existing Compose test (`CompanyItemTest`)

#### ViewModel Gaps
- `HomeViewModel.start()` / channel flow integration — not tested
- `HomeViewModel.exportMetricsCSV()` / `exportMetricsJSON()` — not tested
- Error propagation from failed use cases — not tested
- Throttle strategy adjustment based on list size — not tested

### Priority
HIGH - Core Functionality

### Acceptance Criteria

#### Unit Tests
- [ ] Tests for all 6 domain use cases
- [ ] Tests for all 3 mappers (data→domain, data→UI, domain→UI)
- [ ] Tests for `StockRepository`
- [ ] Tests for `FlowExt.throttleUpdates`
- [ ] Tests for `ThrottleStrategy.forUpdateType()`
- [ ] Tests for `InputValidator`
- [ ] Tests for `Company.updateFromDomain()`
- [ ] Tests for `HomeViewModel.start()` channel integration
- [ ] Tests for `HomeViewModel` export and error flows

#### UI / Instrumented Tests
- [ ] Compose tests for `HomeScreenRoute`
- [ ] Compose tests for `IntervalInput`
- [ ] Expand `CompanyItemTest` coverage

#### Infrastructure
- [ ] Test coverage reporting configured (e.g. Jacoco)
- [ ] Coverage thresholds defined for CI
