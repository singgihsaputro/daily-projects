package com.dailyprojects.countdown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dailyprojects.countdown.ui.theme.CountdownTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = LocalEventRepository(applicationContext)
        setContent {
            CountdownTheme {
                val vm: CountdownViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                        CountdownViewModel(repository) as T
                })
                val state by vm.state.collectAsState()
                CountdownScreen(state)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownScreen(state: CountdownUiState) {
    Scaffold(topBar = { TopAppBar(title = { Text("Countdown") }) }) { padding ->
        if (state.loading) {
            Column(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { CircularProgressIndicator() }
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.upcoming, key = { it.id }) { EventCard(it, state.now) }
            if (state.passed.isNotEmpty()) {
                item { Text("Already happened", style = MaterialTheme.typography.titleMedium) }
                items(state.passed, key = { it.id }) { EventCard(it, state.now) }
            }
        }
    }
}

private val dateFormat = DateTimeFormatter.ofPattern("EEE, d MMM yyyy · HH:mm")

@Composable
private fun EventCard(event: CountdownEvent, now: LocalDateTime) {
    val status = event.statusAt(now)
    val container = when (status) {
        is EventStatus.Upcoming -> MaterialTheme.colorScheme.primaryContainer
        is EventStatus.Passed -> MaterialTheme.colorScheme.surfaceVariant
    }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = container)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(event.emoji, fontSize = 28.sp)
                Column(Modifier.padding(start = 12.dp)) {
                    Text(event.title, style = MaterialTheme.typography.titleMedium)
                    Text(event.date.format(dateFormat), style = MaterialTheme.typography.bodySmall)
                }
            }
            when (status) {
                is EventStatus.Upcoming -> {
                    val r = status.remaining
                    Row(
                        Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TimeBox("${r.days}", "days")
                        TimeBox("%02d".format(r.hours), "hours")
                        TimeBox("%02d".format(r.minutes), "min")
                        TimeBox("%02d".format(r.seconds), "sec")
                    }
                }
                is EventStatus.Passed -> Text(
                    if (status.daysAgo == 0L) "Earlier today" else "${status.daysAgo} days ago",
                    Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun TimeBox(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 30.sp, style = MaterialTheme.typography.headlineMedium)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
