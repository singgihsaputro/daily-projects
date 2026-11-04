package com.dailyprojects.flashcards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dailyprojects.flashcards.ui.FlashcardsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                StudyViewModel(LocalDeckRepository(applicationContext)) as T
        }
        setContent {
            FlashcardsTheme {
                val vm: StudyViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsState()
                StudyScreen(state, vm::flip, vm::answer, vm::restart)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    state: StudyState,
    onFlip: () -> Unit,
    onAnswer: (Boolean) -> Unit,
    onRestart: () -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text(state.deckName.ifEmpty { "Flashcards" }) }) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                state.loading -> CircularProgressIndicator()
                state.finished -> Finished(state, onRestart)
                else -> Studying(state, onFlip, onAnswer)
            }
        }
    }
}

@Composable
private fun Studying(state: StudyState, onFlip: () -> Unit, onAnswer: (Boolean) -> Unit) {
    val card = state.current ?: return
    LinearProgressIndicator(progress = { state.known / state.total.toFloat() }, modifier = Modifier.fillMaxWidth())
    Text(
        "${state.known} of ${state.total} learned · ${state.queue.size} left",
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 8.dp),
    )
    Spacer(Modifier.height(24.dp))
    Card(
        modifier = Modifier.fillMaxWidth().height(260.dp).clickable(onClick = onFlip),
        colors = CardDefaults.cardColors(
            containerColor = if (state.revealed) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(if (state.revealed) "ANSWER" else "QUESTION", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(12.dp))
            Text(
                if (state.revealed) card.back else card.front,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
    Spacer(Modifier.height(24.dp))
    if (state.revealed) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { onAnswer(false) }, modifier = Modifier.weight(1f)) { Text("Again") }
            Button(onClick = { onAnswer(true) }, modifier = Modifier.weight(1f)) { Text("Got it") }
        }
    } else {
        Button(onClick = onFlip, modifier = Modifier.fillMaxWidth()) { Text("Show answer") }
    }
}

@Composable
private fun Finished(state: StudyState, onRestart: () -> Unit) {
    Spacer(Modifier.height(48.dp))
    Text("Deck complete", style = MaterialTheme.typography.headlineMedium)
    Text("You learned all ${state.total} cards.", modifier = Modifier.padding(vertical = 12.dp))
    Button(onClick = onRestart) { Text("Study again") }
}
