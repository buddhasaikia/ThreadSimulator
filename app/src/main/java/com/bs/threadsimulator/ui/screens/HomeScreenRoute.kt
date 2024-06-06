package com.bs.threadsimulator.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bs.threadsimulator.R
import com.bs.threadsimulator.data.MockDataSource
import com.bs.threadsimulator.model.Company
import com.bs.threadsimulator.ui.theme.ThreadSimulatorTheme

@Composable
fun HomeScreenRoute(innerPadding: PaddingValues, homeViewModel: HomeViewModel = hiltViewModel()) {
    HomeScreen(
        innerPadding,
        homeViewModel.companyList,
        onStart = { homeViewModel.start() },
        onStop = { homeViewModel.stop() }
    )
}

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    companyList: List<Company>,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        var started by remember { mutableStateOf(false) }
        Button(
            onClick = {
                if (started) {
                    onStop()
                } else {
                    onStart()
                }
                started = !started
            },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            if (started) {
                Text("Stop")
            } else {
                Text("Start")
            }
        }
        LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
            items(companyList) {
                CompanyItem(it)
                HorizontalDivider(color = Color.Transparent, thickness = 8.dp)
            }
        }
    }
}

@Composable
fun CompanyItem(company: Company) {
    ElevatedCard(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .clickable(enabled = true, onClick = {}),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Text(
            text = company.companyName,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp),
            textDecoration = TextDecoration.Underline
        )
        Text(
            text = "PE: ${company.peRatio}",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
        Text(
            text = "Closing Price: ${company.previousClosingPrice}",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
        Text(
            text = "Price: ${company.stock.currentPrice}",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = colorResource(R.color.purple_200)
        )
        Row {
            Text(
                text = "High: ${company.stock.high}",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                color = colorResource(R.color.green)
            )
            Text(
                text = "Low: ${company.stock.low}",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                color = Color.Red
            )
        }
        Text(
            text = company.threadName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = colorResource(R.color.yellow)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StockListPreview() {
    ThreadSimulatorTheme {
        HomeScreen(
            PaddingValues(2.dp),
            companyList = MockDataSource().getCompanyList(),
            onStart = {},
            onStop = {}
        )
    }
}