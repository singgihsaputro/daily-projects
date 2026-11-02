package com.dailyprojects.shimmer.sample

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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.dailyprojects.shimmer.SkeletonList
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) { ArticleScreen(AssetArticleSource(LocalContext.current)) }
            }
        }
    }
}

@Composable
private fun ArticleScreen(source: ArticleSource) {
    var articles by remember { mutableStateOf<List<Article>?>(null) }
    val scope = rememberCoroutineScope()
    suspend fun refresh() {
        articles = null
        articles = source.load()
    }
    LaunchedEffect(Unit) { refresh() }

    Column(Modifier.fillMaxSize().padding(top = 32.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Articles", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { scope.launch { refresh() } }) { Text("Reload") }
        }
        val loaded = articles
        if (loaded == null) {
            SkeletonList(count = 5)
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(loaded) { a ->
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(a.title, style = MaterialTheme.typography.titleMedium)
                        Text(a.author, style = MaterialTheme.typography.labelMedium)
                        Text(a.summary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
