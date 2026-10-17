package com.dailyprojects.breadcrumbs.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyprojects.breadcrumbs.Breadcrumbs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val source: SessionSource = AssetSessionSource(this)
        setContent { MaterialTheme { Surface(Modifier.fillMaxSize()) { Screen(source) } } }
    }
}

@Composable
private fun Screen(source: SessionSource) {
    var trail by remember { mutableStateOf(Breadcrumbs.recorder.dump()) }
    var lastCrash by remember { mutableStateOf(Breadcrumbs.pendingCrashReport()) }
    var next by remember { mutableStateOf(0) }
    val steps = remember { source.steps() }

    Column(
        Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Crash breadcrumbs", style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val s = steps[next % steps.size]
                Breadcrumbs.log(s.category, s.message, s.level)
                next++
                trail = Breadcrumbs.recorder.dump()
            }) { Text("Replay next step") }
            OutlinedButton(onClick = {
                Breadcrumbs.log("pay", "about to crash on purpose", com.dailyprojects.breadcrumbs.Level.ERROR)
                throw IllegalStateException("Simulated checkout crash")
            }) { Text("Crash now") }
        }
        Text("Trail", style = MaterialTheme.typography.titleMedium)
        Text(trail.ifEmpty { "(empty)" }, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        lastCrash?.let { report ->
            Text("Report from previous launch", style = MaterialTheme.typography.titleMedium)
            Text(report, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            OutlinedButton(onClick = { Breadcrumbs.clearCrashReport(); lastCrash = null }) { Text("Dismiss") }
        }
    }
}
