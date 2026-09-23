package com.dailyprojects.retrybackoff.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel

class RetryDemoViewModelFactory(private val repository: ScenarioRepository) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return RetryDemoViewModel(repository) as T
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = AssetScenarioRepository(applicationContext)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel: RetryDemoViewModel = viewModel(factory = RetryDemoViewModelFactory(repository))
                    RetryDemoScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun RetryDemoScreen(viewModel: RetryDemoViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("retrybackoff demo", style = MaterialTheme.typography.headlineSmall)
        Text("Pick a flaky scenario and watch retryWithBackoff() work through it.")

        state.scenarios.forEach { scenario ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = state.selected == scenario, onClick = { viewModel.select(scenario) })
                Text(scenario.label)
            }
        }

        Button(onClick = { viewModel.run() }, enabled = state.status != RunStatus.RUNNING) {
            Text(if (state.status == RunStatus.RUNNING) "Running…" else "Run")
        }

        Text("Status: ${state.status}")
        state.resultText?.let { Text(it) }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(state.log) { line -> Text("• $line") }
        }
    }
}
