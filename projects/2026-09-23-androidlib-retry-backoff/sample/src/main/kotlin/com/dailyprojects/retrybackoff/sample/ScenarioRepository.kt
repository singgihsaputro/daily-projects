package com.dailyprojects.retrybackoff.sample

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException

data class ApiScenario(
    val id: String,
    val label: String,
    val failuresBeforeSuccess: Int,
    val errorMessage: String
)

/** Where the flaky scenarios come from. Swap in a real backend by changing one file. */
interface ScenarioRepository {
    suspend fun loadScenarios(): List<ApiScenario>
}

/** Reads scenarios from the bundled `mock/scenarios.json` asset. */
class AssetScenarioRepository(private val context: Context) : ScenarioRepository {
    override suspend fun loadScenarios(): List<ApiScenario> = withContext(Dispatchers.IO) {
        val text = context.assets.open("mock/scenarios.json").bufferedReader().use { it.readText() }
        val array = JSONArray(text)
        (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            ApiScenario(
                id = obj.getString("id"),
                label = obj.getString("label"),
                failuresBeforeSuccess = obj.getInt("failuresBeforeSuccess"),
                errorMessage = obj.getString("errorMessage")
            )
        }
    }
}

/** A fake flaky endpoint: fails [ApiScenario.failuresBeforeSuccess] times, then succeeds. */
class MockFlakyApi(private val scenario: ApiScenario) {
    private var callCount = 0

    suspend fun call(): String {
        callCount++
        if (callCount <= scenario.failuresBeforeSuccess) {
            throw IOException(scenario.errorMessage)
        }
        return "Loaded payload for '${scenario.label}' on attempt $callCount"
    }
}
