package com.bs.threadsimulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import com.bs.threadsimulator.ui.screens.HomeScreenRoute
import com.bs.threadsimulator.ui.theme.ThreadSimulatorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThreadSimulatorTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("Thread Simulator") })
                    }) { innerPadding ->
                    val mainViewModel: MainViewModel by viewModels()
                    HomeScreenRoute(innerPadding, mainViewModel)
                }
            }
        }
    }
}