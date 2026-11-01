package com.dailyprojects.notestore.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dailyprojects.notestore.NoteView

@Composable
fun NoteScreen(viewModel: NoteViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Note store", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Pointed at server/ on http://10.0.2.2:8080 — start it with ./gradlew :server:run",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            label = { Text("Search notes") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.titleInput,
            onValueChange = viewModel::onTitleChange,
            label = { Text("New note title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            OutlinedTextField(
                value = state.bodyInput,
                onValueChange = viewModel::onBodyChange,
                label = { Text("Body") },
                modifier = Modifier.weight(1f),
            )
            Button(onClick = viewModel::add, modifier = Modifier.padding(start = 8.dp)) { Text("Add") }
        }

        state.errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        if (state.isLoading && state.notes.isEmpty()) {
            Text("Loading...", modifier = Modifier.padding(top = 16.dp))
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.notes, key = { it.note.id }) { view ->
                NoteCard(view, onPin = { viewModel.pin(view.note.id) }, onRemove = { viewModel.remove(view.note.id) })
            }
        }
    }
}

@Composable
private fun NoteCard(view: NoteView, onPin: () -> Unit, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                (if (view.note.pinned) "📌 " else "") + view.note.title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(view.preview, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            Row {
                TextButton(onClick = onPin) { Text(if (view.note.pinned) "Unpin" else "Pin") }
                TextButton(onClick = onRemove) { Text("Delete") }
            }
        }
    }
}
