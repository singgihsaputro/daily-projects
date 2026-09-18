package com.dailyprojects.unitconverter.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dailyprojects.unitconverter.AndroidUnitJsonSource
import com.dailyprojects.unitconverter.ConverterModel
import com.dailyprojects.unitconverter.MockUnitRepository
import com.dailyprojects.unitconverter.app.ui.theme.UnitConverterTheme

class MainActivity : ComponentActivity() {

    private val model = ConverterModel(
        repository = MockUnitRepository(AndroidUnitJsonSource(this)),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model.load()
        setContent {
            UnitConverterTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ConverterScreen(model)
                }
            }
        }
    }
}
