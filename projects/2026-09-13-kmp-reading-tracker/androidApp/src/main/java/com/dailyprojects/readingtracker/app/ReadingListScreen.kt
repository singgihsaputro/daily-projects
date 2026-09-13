package com.dailyprojects.readingtracker.app

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailyprojects.readingtracker.Book
import com.dailyprojects.readingtracker.ReadingListModel
import com.dailyprojects.readingtracker.ReadingStatus
import com.dailyprojects.readingtracker.currentPlatformLabel

@Composable
fun ReadingListScreen(model: ReadingListModel) {
    val state by model.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text(
            text = "Reading Tracker",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "${state.booksFinished} finished · ${state.pagesReadTotal} pages read",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Shared engine running on: ${currentPlatformLabel()}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
        )

        Spacer(Modifier.height(16.dp))

        if (state.isLoading) {
            Text("Loading books...")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.books, key = { it.id }) { book ->
                    BookCard(book = book, onAddPages = { model.addPages(book.id, 25) })
                }
            }
        }
    }
}

@Composable
private fun BookCard(book: Book, onAddPages: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(book.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(book.author, style = MaterialTheme.typography.bodySmall)
                }
                StatusBadge(book.status)
            }

            Spacer(Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { book.progress },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${book.pagesRead} / ${book.totalPages} pages",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (book.status != ReadingStatus.FINISHED) {
                    Button(onClick = onAddPages) { Text("+25 pages") }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ReadingStatus) {
    val label = when (status) {
        ReadingStatus.WANT_TO_READ -> "Want to read"
        ReadingStatus.READING -> "Reading"
        ReadingStatus.FINISHED -> "Finished"
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
