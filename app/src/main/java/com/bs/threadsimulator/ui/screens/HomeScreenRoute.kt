package com.bs.threadsimulator.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bs.threadsimulator.common.ThreadMetrics
import com.bs.threadsimulator.data.MockDataSource
import com.bs.threadsimulator.model.Company
import com.bs.threadsimulator.model.toCompany
import com.bs.threadsimulator.ui.screens.components.CompanyItem
import com.bs.threadsimulator.ui.screens.components.IntervalInput
import com.bs.threadsimulator.ui.theme.ThreadSimulatorTheme

@Composable
fun HomeScreenRoute(innerPadding: PaddingValues, homeViewModel: HomeViewModel = hiltViewModel()) {
    val threadMetrics by homeViewModel.threadMetrics.collectAsState()

    HomeScreen(
        innerPadding,
        homeViewModel.companyList,
        threadMetrics = threadMetrics,
        populateList = { homeViewModel.populateList(it) },
        onStart = { homeViewModel.start() },
        onStop = { homeViewModel.stop() },
        onSetUpdateInterval = { name, interval ->
            homeViewModel.setUpdateInterval(name, interval)
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    companyList: List<Company>,
    threadMetrics: List<ThreadMetrics>,
    onSetUpdateInterval: (String, Long) -> Unit,
    populateList: (Int) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Text(
            text = "Total workers:${threadMetrics.count()}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        // Add thread metrics display
        ThreadMetricsDisplay(threadMetrics)

        Text(
            text = "Set update intervals below (Milliseconds):",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            var peUpdateIntervalInMs by remember { mutableStateOf("1500") }
            IntervalInput(
                label = "PE (ms)",
                value = peUpdateIntervalInMs,
                onValueChange = { value ->
                    peUpdateIntervalInMs = value
                    if (value.isBlank()) return@IntervalInput
                    onSetUpdateInterval("PE", peUpdateIntervalInMs.trim().toLong())
                },
                modifier = Modifier
                    .weight(2f)
                    .padding(start = 8.dp, end = 8.dp)
            )
            var currentPriceUpdateIntervalInMs by remember { mutableStateOf("1000") }
            IntervalInput(
                label = "Price (ms)",
                value = currentPriceUpdateIntervalInMs,
                onValueChange = { value ->
                    currentPriceUpdateIntervalInMs = value
                    if (value.isBlank()) return@IntervalInput
                    onSetUpdateInterval(
                        "current_price",
                        currentPriceUpdateIntervalInMs.trim().toLong()
                    )
                },
                modifier = Modifier
                    .weight(2f)
                    .padding(end = 8.dp)
            )
            var highLowUpdateIntervalInMs by remember { mutableStateOf("1000") }
            IntervalInput(
                label = "High/Low (ms)",
                value = highLowUpdateIntervalInMs,
                onValueChange = { value ->
                    highLowUpdateIntervalInMs = value
                    if (value.isBlank()) return@IntervalInput
                    onSetUpdateInterval("high_low", highLowUpdateIntervalInMs.trim().toLong())
                },
                modifier = Modifier
                    .weight(2f)
                    .padding(end = 8.dp)
            )
        }
        var started by remember { mutableStateOf(false) }
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            var listSize by remember { mutableStateOf("5") }
            IntervalInput(
                label = "List Size",
                value = listSize,
                onValueChange = { value ->
                    listSize = value
                },
                modifier = Modifier
                    .padding(end = 10.dp)
                    .weight(2f)
            )
            Button(
                shape = RoundedCornerShape(4.dp),
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    if (listSize.isBlank()) return@Button
                    populateList(listSize.trim().toInt())
                },
                modifier = Modifier
                    .height(60.dp)
                    .padding(top = 4.dp, end = 8.dp)
                    .weight(2f)
            ) {
                Text("Populate", fontSize = 16.sp)
            }
            Button(
                shape = RoundedCornerShape(4.dp),
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    if (started) {
                        onStop()
                    } else {
                        onStart()
                    }
                    started = !started
                },
                modifier = Modifier
                    .height(60.dp)
                    .padding(top = 4.dp)
                    .weight(2f)
            ) {
                if (started) {
                    Text("Stop", fontSize = 16.sp)
                } else {
                    Text("Start", fontSize = 16.sp)
                }
            }
        }
        LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
            items(
                items = companyList,
                key = { it.stock.symbol }, // Stable key for better recomposition
                contentType = { it.categoryIndex } // Help Compose optimize similar items
            ) { company ->
                CompanyItem(
                    company = company,
                    modifier = Modifier.animateItem() // Removed animateItemPlacement for compatibility
                )
                HorizontalDivider(color = Color.Transparent, thickness = 8.dp)
            }
        }
    }
}

@Composable
private fun ThreadMetricsDisplay(threadMetrics: List<ThreadMetrics>) {
    if (threadMetrics.isEmpty()) return

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        items(threadMetrics) { metric ->
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .width(200.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Thread: ${metric.threadName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Type: ${metric.updateType}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Updates: ${metric.updateCount}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Avg Time: ${metric.avgUpdateTimeMs}ms",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StockListPreview() {
    ThreadSimulatorTheme {
        HomeScreen(
            PaddingValues(2.dp),
            companyList = MockDataSource().getCompanyList().map { it.toCompany() },
            threadMetrics = listOf(),
            populateList = {},
            onSetUpdateInterval = { _, _ -> },
            onStart = {},
            onStop = {}
        )
    }
}