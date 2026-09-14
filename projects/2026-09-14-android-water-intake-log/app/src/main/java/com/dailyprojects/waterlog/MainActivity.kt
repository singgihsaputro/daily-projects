package com.dailyprojects.waterlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.dailyprojects.waterlog.ui.theme.WaterLogTheme

class MainActivity : ComponentActivity() {

    private val viewModel: WaterLogViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return WaterLogViewModel(LocalWaterLogRepository(applicationContext)) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WaterLogTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WaterLogScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun WaterLogScreen(viewModel: WaterLogViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            Text(
                text = "Water Intake",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "${state.totalMl} / ${state.goalMl} ml",
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
            )

            Spacer(Modifier.height(24.dp))

            Text(text = "Add a drink", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.presets) { preset ->
                    AssistChip(
                        onClick = { viewModel.addEntry(preset) },
                        label = { Text("${preset.label} · ${preset.amountMl} ml") },
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(text = "Today's log", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (state.entries.isEmpty()) {
                Text(
                    text = "Nothing logged yet today.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                LazyColumn {
                    items(state.entries, key = { it.id }) { entry ->
                        WaterEntryRow(entry = entry, onDelete = { viewModel.removeEntry(entry.id) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun WaterEntryRow(entry: WaterEntry, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(text = "${entry.label} · ${entry.amountMl} ml", style = MaterialTheme.typography.bodyLarge)
            Text(text = entry.time, style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onDelete) {
            Text("✕")
        }
    }
}
