package com.dailyprojects.grocerylist.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dailyprojects.grocerylist.AndroidGroceryJsonSource
import com.dailyprojects.grocerylist.GroceryListModel
import com.dailyprojects.grocerylist.MockGroceryRepository
import com.dailyprojects.grocerylist.app.ui.theme.GroceryListTheme

class MainActivity : ComponentActivity() {

    private val model = GroceryListModel(
        repository = MockGroceryRepository(AndroidGroceryJsonSource(this)),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model.load()
        setContent {
            GroceryListTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GroceryListScreen(model)
                }
            }
        }
    }
}
