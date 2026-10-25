package com.dailyprojects.redactlog.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import com.dailyprojects.redactlog.LogLevel
import com.dailyprojects.redactlog.LogSink
import com.dailyprojects.redactlog.RedactingLogger
import com.dailyprojects.redactlog.Redactor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val raw = AssetLineSource(this).lines()
        val redactor = Redactor()
        // The same logger an app would use; the sink collects what logcat would receive.
        val captured = mutableListOf<String>()
        val logger = RedactingLogger(LogSink { _: LogLevel, _: String, m: String -> captured += m }, redactor)
        raw.forEach { logger.i("demo", it) }

        setContent {
            MaterialTheme {
                var redacted by remember { mutableStateOf(true) }
                val shown = if (redacted) captured else raw
                Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("RedactLog", style = MaterialTheme.typography.headlineSmall)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Switch(checked = redacted, onCheckedChange = { redacted = it })
                        Text(if (redacted) "What logcat receives" else "Raw input (never logged)")
                    }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(shown) { line ->
                            Card { Text(line, Modifier.padding(12.dp), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }
        }
    }
}
