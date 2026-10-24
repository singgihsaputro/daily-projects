package com.dailyprojects.habitsync.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dailyprojects.habitsync.HabitView

@Composable
fun HabitScreen(viewModel: HabitViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Habit sync", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Pointed at server/ on http://10.0.2.2:8080 — start it with ./gradlew :server:run",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.nameInput,
                onValueChange = viewModel::onNameChange,
                label = { Text("New habit") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = state.goalInput,
                onValueChange = viewModel::onGoalChange,
                label = { Text("Days/wk") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(92.dp).padding(start = 8.dp),
            )
            Button(onClick = viewModel::add, modifier = Modifier.padding(start = 8.dp)) { Text("Add") }
        }

        state.errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        if (state.isLoading && state.habits.isEmpty()) {
            Text("Loading...", modifier = Modifier.padding(top = 16.dp))
            return@Column
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            items(state.habits, key = { it.habit.id }) { view ->
                HabitRow(view, onToggle = { viewModel.toggleToday(view.habit.id) }, onRemove = { viewModel.remove(view.habit.id) })
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun HabitRow(view: HabitView, onToggle: () -> Unit, onRemove: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = view.doneToday, onCheckedChange = { onToggle() })
        Column(modifier = Modifier.weight(1f)) {
            Text(view.habit.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${view.streak}-day streak · ${view.doneThisWeek}/${view.habit.goalPerWeek} this week" +
                    if (view.goalMet) " ✓" else "",
                style = MaterialTheme.typography.labelMedium,
                color = if (view.goalMet) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
            )
        }
        TextButton(onClick = onRemove) { Text("Delete") }
    }
}
