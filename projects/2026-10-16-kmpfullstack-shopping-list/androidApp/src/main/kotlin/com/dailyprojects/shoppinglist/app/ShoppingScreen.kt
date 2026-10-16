package com.dailyprojects.shoppinglist.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dailyprojects.shoppinglist.ShoppingItem

@Composable
fun ShoppingScreen(viewModel: ShoppingViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Shopping list", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Pointed at server/ on http://10.0.2.2:8080 — start it with ./gradlew :server:run",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.nameInput,
                onValueChange = viewModel::onNameChange,
                label = { Text("Item") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = state.quantityInput,
                onValueChange = viewModel::onQuantityChange,
                label = { Text("Qty") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(80.dp).padding(start = 8.dp),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.categoryInput,
                onValueChange = viewModel::onCategoryChange,
                label = { Text("Category (optional)") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = viewModel::add, modifier = Modifier.padding(start = 8.dp)) { Text("Add") }
        }

        state.errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        CategoryFilter(state.categories, state.selectedCategory, viewModel::selectCategory)

        if (state.isLoading && state.items.isEmpty()) {
            Text("Loading...", modifier = Modifier.padding(top = 16.dp))
            return@Column
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.items, key = { it.id }) { item ->
                ItemRow(item, onCheck = { viewModel.check(item.id) }, onRemove = { viewModel.remove(item.id) })
                HorizontalDivider()
            }
        }
        if (state.items.any { it.checked }) {
            TextButton(onClick = viewModel::clearTicked) { Text("Clear ticked items") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilter(categories: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
        item { FilterChip(selected = selected.isEmpty(), onClick = { onSelect("") }, label = { Text("All") }) }
        items(categories) { category ->
            FilterChip(selected = selected == category, onClick = { onSelect(category) }, label = { Text(category) })
        }
    }
}

@Composable
private fun ItemRow(item: ShoppingItem, onCheck: () -> Unit, onRemove: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = item.checked, onCheckedChange = { onCheck() })
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${item.name} × ${item.quantity}",
                style = MaterialTheme.typography.titleMedium,
                textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
            )
            Text(
                item.category.ifEmpty { "uncategorised" },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        TextButton(onClick = onRemove) { Text("Delete") }
    }
}
