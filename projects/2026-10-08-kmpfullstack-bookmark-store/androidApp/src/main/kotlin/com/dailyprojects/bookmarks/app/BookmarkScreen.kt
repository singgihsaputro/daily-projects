package com.dailyprojects.bookmarks.app

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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dailyprojects.bookmarks.Bookmark

@Composable
fun BookmarkScreen(viewModel: BookmarkViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Bookmarks", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Pointed at server/ on http://10.0.2.2:8080 — start it with ./gradlew :server:run",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.titleInput,
            onValueChange = viewModel::onTitleChange,
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.urlInput,
            onValueChange = viewModel::onUrlChange,
            label = { Text("https://…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.tagInput,
                onValueChange = viewModel::onTagChange,
                label = { Text("Tag (optional)") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = viewModel::add, modifier = Modifier.padding(start = 8.dp)) { Text("Save") }
        }

        state.errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        TagFilter(tags = state.tags, selected = state.selectedTag, onSelect = viewModel::selectTag)

        if (state.isLoading && state.bookmarks.isEmpty()) {
            Text("Loading...", modifier = Modifier.padding(top = 16.dp))
            return@Column
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.bookmarks, key = { it.id }) { bookmark ->
                BookmarkRow(
                    bookmark,
                    onStar = { viewModel.star(bookmark.id) },
                    onRemove = { viewModel.remove(bookmark.id) },
                )
                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagFilter(tags: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
        item { FilterChip(selected = selected.isEmpty(), onClick = { onSelect("") }, label = { Text("All") }) }
        items(tags) { tag ->
            FilterChip(selected = selected == tag, onClick = { onSelect(tag) }, label = { Text("#$tag") })
        }
    }
}

@Composable
private fun BookmarkRow(bookmark: Bookmark, onStar: () -> Unit, onRemove: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(bookmark.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(bookmark.url, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (bookmark.tag.isEmpty()) "untagged" else "#${bookmark.tag}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onStar) { Text(if (bookmark.starred) "★ Starred" else "☆ Star") }
            TextButton(onClick = onRemove) { Text("Delete") }
        }
    }
}
