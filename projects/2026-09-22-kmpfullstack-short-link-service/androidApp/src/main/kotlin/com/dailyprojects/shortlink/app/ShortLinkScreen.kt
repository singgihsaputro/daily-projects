package com.dailyprojects.shortlink.app

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.dailyprojects.shortlink.ShortLink

@Composable
fun ShortLinkScreen(viewModel: ShortLinkViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Short Links", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Pointed at server/ on http://10.0.2.2:8080 — start it with ./gradlew :server:run",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.newUrlInput,
                onValueChange = viewModel::onNewUrlChange,
                label = { Text("Paste a URL to shorten") },
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.height(0.dp))
            Button(onClick = viewModel::addLink, modifier = Modifier.padding(start = 8.dp)) {
                Text("Shorten")
            }
        }

        state.errorMessage?.let { message ->
            Spacer(Modifier.height(8.dp))
            Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${state.links.size} links", style = MaterialTheme.typography.labelLarge)
            TextButton(onClick = viewModel::refresh) { Text("Refresh") }
        }

        if (state.isLoading && state.links.isEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Loading...")
            return@Column
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.links, key = { it.code }) { link ->
                ShortLinkRow(link, onVisit = { viewModel.visit(link.code) })
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun ShortLinkRow(link: ShortLink, onVisit: () -> Unit) {
    Surface(onClick = onVisit, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("/r/${link.code}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(link.longUrl, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Text("${link.clicks} clicks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}
