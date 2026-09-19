package com.dailyprojects.pwgen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.dailyprojects.pwgen.ui.theme.PasswordGeneratorTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PasswordGeneratorViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return PasswordGeneratorViewModel(LocalPasswordHistoryRepository(applicationContext)) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PasswordGeneratorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PasswordGeneratorScreen(viewModel)
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, password: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("password", password))
}

private fun strengthColor(strength: String): Color = when (strength) {
    "Weak" -> Color(0xFFC62828)
    "Fair" -> Color(0xFFF9A825)
    "Strong" -> Color(0xFF2E7D32)
    else -> Color(0xFF1565C0)
}

@Composable
fun PasswordGeneratorScreen(viewModel: PasswordGeneratorViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            Text(
                text = "Password Generator",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = state.password,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Monospace,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = state.strength,
                style = MaterialTheme.typography.labelLarge,
                color = strengthColor(state.strength),
            )

            state.error?.let {
                Spacer(Modifier.height(4.dp))
                Text(text = it, color = strengthColor("Weak"), style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { viewModel.generate() }) { Text("Generate") }
                OutlinedButton(
                    onClick = { copyToClipboard(context, state.password) },
                    enabled = state.password.isNotEmpty(),
                ) { Text("Copy") }
            }

            Spacer(Modifier.height(24.dp))

            Text(text = "Length: ${state.options.length}", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = state.options.length.toFloat(),
                onValueChange = { viewModel.setLength(it.toInt()) },
                onValueChangeFinished = { viewModel.generate() },
                valueRange = 8f..32f,
                steps = 23,
            )

            val toggles = listOf(
                Triple("Uppercase (A-Z)", state.options.useUppercase, viewModel::toggleUppercase),
                Triple("Lowercase (a-z)", state.options.useLowercase, viewModel::toggleLowercase),
                Triple("Numbers (0-9)", state.options.useNumbers, viewModel::toggleNumbers),
                Triple("Symbols (!@#$)", state.options.useSymbols, viewModel::toggleSymbols),
            )
            toggles.forEach { (label, checked, toggle) ->
                OptionRow(label, checked) {
                    toggle(it)
                    viewModel.generate()
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(text = "History", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(state.history, key = { it.id }) { entry ->
                    HistoryRow(entry)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun OptionRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(text = entry.password, style = MaterialTheme.typography.bodyLarge, fontFamily = FontFamily.Monospace)
            Text(text = "${entry.strength} · ${entry.length} chars · ${entry.time}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
