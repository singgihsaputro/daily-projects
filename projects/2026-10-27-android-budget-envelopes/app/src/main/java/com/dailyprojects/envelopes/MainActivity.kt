package com.dailyprojects.envelopes

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dailyprojects.envelopes.ui.theme.EnvelopesTheme

private val STEPS = listOf(5, 10, 25)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                BudgetViewModel(LocalBudgetRepository(applicationContext)) as T
        }
        setContent {
            EnvelopesTheme {
                val vm: BudgetViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsState()
                BudgetScreen(state, vm::spend, vm::refund)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(state: BudgetUiState, onSpend: (String, Int) -> Unit, onRefund: (String, Int) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("${state.month} envelopes") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Summary(state) }
            items(state.envelopes, key = { it.id }) { EnvelopeCard(it, onSpend, onRefund) }
        }
    }
}

@Composable
private fun Summary(state: BudgetUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("$${state.totalLeft} left of $${state.totalBudget}", style = MaterialTheme.typography.headlineSmall)
            Text("${state.daysLeft} days to go · safe to spend $${state.safePerDay}/day")
        }
    }
}

@Composable
private fun statusColor(status: Status): Color = when (status) {
    Status.OK -> MaterialTheme.colorScheme.primary
    Status.NEAR_LIMIT -> MaterialTheme.colorScheme.secondary
    Status.EMPTY -> MaterialTheme.colorScheme.outline
    Status.OVER -> MaterialTheme.colorScheme.error
}

@Composable
private fun EnvelopeCard(envelope: Envelope, onSpend: (String, Int) -> Unit, onRefund: (String, Int) -> Unit) {
    val color = statusColor(envelope.status)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(envelope.name, style = MaterialTheme.typography.titleMedium)
                val label = if (envelope.left < 0) "over by $${-envelope.left}" else "$${envelope.left} left"
                Text(label, color = color, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(progress = { envelope.fraction }, color = color, modifier = Modifier.fillMaxWidth())
            Text("$${envelope.spent} of $${envelope.budget}", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                STEPS.forEach { step ->
                    FilterChip(
                        selected = false,
                        onClick = { onSpend(envelope.id, step) },
                        label = { Text("+$$step") },
                    )
                }
                FilterChip(
                    selected = false,
                    onClick = { onRefund(envelope.id, STEPS.first()) },
                    label = { Text("−$${STEPS.first()}") },
                )
            }
        }
    }
}
