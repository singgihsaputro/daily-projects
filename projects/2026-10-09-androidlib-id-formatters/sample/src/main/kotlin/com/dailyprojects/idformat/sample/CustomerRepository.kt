package com.dailyprojects.idformat.sample

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

data class Customer(val name: String, val phone: String, val nik: String, val balance: Long)

/** Where customers come from. Swap in a real backend by changing one file. */
interface CustomerRepository {
    suspend fun load(): List<Customer>
}

/** Reads customers from the bundled `mock/customers.json` asset. */
class AssetCustomerRepository(private val context: Context) : CustomerRepository {
    override suspend fun load(): List<Customer> = withContext(Dispatchers.IO) {
        val text = context.assets.open("mock/customers.json").bufferedReader().use { it.readText() }
        val array = JSONArray(text)
        (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            Customer(o.getString("name"), o.getString("phone"), o.getString("nik"), o.getLong("balance"))
        }
    }
}
