package com.dailyprojects.caffeine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dailyprojects.caffeine.ui.theme.CaffeineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = viewModelFactory {
            initializer { CaffeineViewModel(LocalCaffeineRepository(applicationContext)) }
        }
        setContent {
            CaffeineTheme {
                val vm: CaffeineViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsStateWithLifecycle()
                CaffeineScreen(state, vm::add, vm::remove)
            }
        }
    }
}

private fun clock(minutesAgo: Int): String =
    if (minutesAgo == 0) "just now" else "${minutesAgo / 60}h ${minutesAgo % 60}m ago"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaffeineScreen(state: CaffeineUiState, onAdd: (Drink) -> Unit, onRemove: (Entry) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Caffeine Tracker") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Summary(state) }
            item { Text("Quick add", style = MaterialTheme.typography.titleMedium) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.drinks, key = { it.id }) { drink ->
                        AssistChip(onClick = { onAdd(drink) }, label = { Text("${drink.name} ${drink.mg} mg") })
                    }
                }
            }
            item { Text("Today", style = MaterialTheme.typography.titleMedium) }
            items(state.log) { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(entry.drink.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${entry.drink.mg} mg · ${clock(entry.minutesAgo)}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    TextButton(onClick = { onRemove(entry) }) { Text("Remove") }
                }
            }
        }
    }
}

@Composable
private fun Summary(state: CaffeineUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${state.activeMg} mg in your system", style = MaterialTheme.typography.headlineSmall)
            val sleep = state.sleepSafeInMinutes
            Text(
                if (sleep == 0) "Low enough for sleep now" else "Sleep-safe in ${sleep / 60}h ${sleep % 60}m",
                style = MaterialTheme.typography.bodyMedium,
            )
            LinearProgressIndicator(
                progress = { if (state.limitMg == 0) 0f else (state.totalMg.toFloat() / state.limitMg).coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "${state.totalMg} / ${state.limitMg} mg today" + if (state.overLimit) " — over the limit" else "",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
