package com.dailyprojects.idformat.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dailyprojects.idformat.Nik
import com.dailyprojects.idformat.PhoneNumber
import com.dailyprojects.idformat.Rupiah

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository: CustomerRepository = AssetCustomerRepository(applicationContext)
        setContent { MaterialTheme { Surface { CustomerList(repository) } } }
    }
}

@Composable
private fun CustomerList(repository: CustomerRepository) {
    var customers by remember { mutableStateOf(emptyList<Customer>()) }
    LaunchedEffect(repository) { customers = repository.load() }

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(customers) { CustomerCard(it) }
    }
}

@Composable
private fun CustomerCard(c: Customer) {
    val phone = PhoneNumber.parse(c.phone)
    val nik = Nik.parse(c.nik)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(c.name, style = MaterialTheme.typography.titleMedium)
            Text("Saldo ${Rupiah.format(c.balance)} (${Rupiah.formatCompact(c.balance)})")
            Text(
                if (phone != null) "${phone.display} · ${phone.operator ?: "operator tidak dikenal"} · ${phone.e164}"
                else "Nomor HP tidak valid: ${c.phone}"
            )
            Text(
                if (nik != null) "NIK ${Nik.mask(c.nik)} · ${nik.provinceName} · ${nik.birthDate} · ${nik.gender}"
                else "NIK tidak valid: ${Nik.mask(c.nik)}"
            )
        }
    }
}
