package com.dailyprojects.readingtracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.dailyprojects.readingtracker.AndroidBookJsonSource
import com.dailyprojects.readingtracker.MockBookRepository
import com.dailyprojects.readingtracker.ReadingListModel
import com.dailyprojects.readingtracker.app.ui.theme.ReadingTrackerTheme

class MainActivity : ComponentActivity() {

    private val model = ReadingListModel(
        repository = MockBookRepository(AndroidBookJsonSource(this)),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model.load()
        setContent {
            ReadingTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ReadingListScreen(model)
                }
            }
        }
    }
}
