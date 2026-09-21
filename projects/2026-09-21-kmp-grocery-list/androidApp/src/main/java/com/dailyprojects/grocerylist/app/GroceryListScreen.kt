package com.dailyprojects.grocerylist.app

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.dailyprojects.grocerylist.GroceryCategory
import com.dailyprojects.grocerylist.GroceryItem
import com.dailyprojects.grocerylist.GroceryListModel
import com.dailyprojects.grocerylist.formatProgress

@Composable
fun GroceryListScreen(model: GroceryListModel) {
    val state by model.state.collectAsState()
    var newItemName by remember { mutableStateOf("") }
    var newItemCategory by remember { mutableStateOf(GroceryCategory.PRODUCE) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text(
            text = "Grocery List",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        if (state.isLoading) {
            Spacer(Modifier.height(16.dp))
            Text("Loading list...")
            return@Column
        }

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("${state.checkedCount} of ${state.items.size} in cart")
            TextButton(onClick = model::clearChecked) { Text("Clear checked") }
        }
        LinearProgressIndicator(
            progress = { state.progress.toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(formatProgress(state.progress), style = MaterialTheme.typography.labelMedium)

        Spacer(Modifier.height(16.dp))
        AddItemRow(
            name = newItemName,
            onNameChange = { newItemName = it },
            category = newItemCategory,
            onCategoryChange = { newItemCategory = it },
            onAdd = {
                model.addItem(newItemName, newItemCategory)
                newItemName = ""
            },
        )

        Spacer(Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            state.grouped.forEach { (category, categoryItems) ->
                item(key = "header-$category") {
                    Text(
                        text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    )
                }
                items(categoryItems, key = { it.id }) { groceryItem ->
                    GroceryRow(groceryItem, onToggle = { model.toggle(groceryItem.id) }, onRemove = { model.removeItem(groceryItem.id) })
                }
            }
        }
    }
}

@Composable
private fun GroceryRow(item: GroceryItem, onToggle: () -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Checkbox(checked = item.checked, onCheckedChange = { onToggle() })
        Text(
            text = "${item.name} (${item.quantity})",
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (item.checked) TextDecoration.LineThrough else null,
            color = if (item.checked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onRemove) {
            Text("✕", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemRow(
    name: String,
    onNameChange: (String) -> Unit,
    category: GroceryCategory,
    onCategoryChange: (GroceryCategory) -> Unit,
    onAdd: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Add item") },
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.width(140.dp),
        ) {
            OutlinedTextField(
                value = category.name.lowercase().replaceFirstChar { it.uppercase() },
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                GroceryCategory.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            onCategoryChange(option)
                            expanded = false
                        },
                    )
                }
            }
        }
        IconButton(onClick = onAdd) {
            Text("+", style = MaterialTheme.typography.headlineSmall)
        }
    }
}
