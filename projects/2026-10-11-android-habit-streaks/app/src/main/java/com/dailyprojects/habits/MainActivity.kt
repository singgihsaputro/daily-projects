package com.dailyprojects.habits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dailyprojects.habits.ui.theme.HabitsTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = LocalHabitRepository(applicationContext)
        setContent {
            HabitsTheme {
                val vm: HabitViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                        HabitViewModel(repository) as T
                })
                val state by vm.state.collectAsState()
                HabitScreen(state, onToggleToday = vm::toggleToday)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(state: HabitUiState, onToggleToday: (Int) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Habit Streaks") }) }) { padding ->
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "${state.doneToday} of ${state.habits.size} done today",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            items(state.habits, key = { it.id }) { HabitCard(it, state.today, onToggleToday) }
        }
    }
}

@Composable
private fun HabitCard(habit: Habit, today: LocalDate, onToggleToday: (Int) -> Unit) {
    val streak = habit.streak(today)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(habit.emoji, fontSize = 28.sp)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(habit.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "🔥 $streak day streak · best ${habit.bestStreak()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (streak > 0) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                for (offset in 6L downTo 0L) {
                    val day = today.minusDays(offset)
                    DayDot(day, habit.isDone(day), isToday = offset == 0L) {
                        onToggleToday(habit.id)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayDot(day: LocalDate, done: Boolean, isToday: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            day.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
            style = MaterialTheme.typography.labelSmall,
        )
        Box(
            Modifier
                .padding(top = 4.dp)
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    if (done) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                )
                .then(if (isToday) Modifier.clickable(onClick = onClick) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            if (done) Text("✓", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
