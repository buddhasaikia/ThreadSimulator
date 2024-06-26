package com.bs.threadsimulator.ui.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bs.threadsimulator.R
import com.bs.threadsimulator.model.Company

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
        Row {
            Text(
                text = company.stock.symbol,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                textDecoration = TextDecoration.Underline
            )
            Text(
                text = "(${company.companyName})",
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                textDecoration = TextDecoration.Underline
            )
        }
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
