package com.dailyprojects.qrgen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.dailyprojects.qrgen.ui.theme.QrGeneratorTheme

class MainActivity : ComponentActivity() {

    private val viewModel: QrGeneratorViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return QrGeneratorViewModel(LocalQrHistoryRepository(applicationContext)) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QrGeneratorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    QrGeneratorScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun QrGeneratorScreen(viewModel: QrGeneratorViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            Text(
                text = "QR Generator",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.input,
                onValueChange = { viewModel.setInput(it) },
                label = { Text("Text or URL") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.presets, key = { it.label }) { preset ->
                    AssistChip(onClick = { viewModel.applyPreset(preset) }, label = { Text(preset.label) })
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(onClick = { viewModel.generate() }) { Text("Generate QR code") }

            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(text = it, color = Color(0xFFC62828), style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))

            state.qrBitmap?.let { bitmap ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .border(1.dp, Color(0xFFBDBDBD)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(bitmap = bitmap, contentDescription = "Generated QR code for ${state.input}")
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(text = "History", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(state.history, key = { it.id }) { entry ->
                    HistoryRow(entry) { viewModel.applyPreset(QrPreset(entry.content, entry.content)) }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: QrHistoryEntry, onReuse: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(text = entry.content, style = MaterialTheme.typography.bodyLarge, fontFamily = FontFamily.Monospace)
            Text(text = entry.time, style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = onReuse) { Text("Reuse") }
    }
}
