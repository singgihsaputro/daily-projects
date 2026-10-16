package com.dailyprojects.shoppinglist.web

import com.dailyprojects.shoppinglist.AddItemUseCase
import com.dailyprojects.shoppinglist.ClearCheckedUseCase
import com.dailyprojects.shoppinglist.ListItemsUseCase
import com.dailyprojects.shoppinglist.RemoveItemUseCase
import com.dailyprojects.shoppinglist.ShopResult
import com.dailyprojects.shoppinglist.ShoppingItem
import com.dailyprojects.shoppinglist.ToggleCheckedUseCase
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event

private val repository = HttpShoppingRepository()
private val listItems = ListItemsUseCase(repository)
private val addItem = AddItemUseCase(repository)
private val toggleChecked = ToggleCheckedUseCase(repository)
private val removeItem = RemoveItemUseCase(repository)
private val clearChecked = ClearCheckedUseCase(repository)
private val scope = MainScope()

private var selectedCategory = ""

private fun element(id: String) = document.getElementById(id) as HTMLElement
private fun input(id: String) = document.getElementById(id) as HTMLInputElement

fun main() {
    (document.getElementById("create-form") as HTMLFormElement).addEventListener("submit", { event: Event ->
        event.preventDefault()
        scope.launch {
            val quantity = input("quantity-input").value.toIntOrNull() ?: 0
            when (val result = addItem(input("name-input").value, quantity, input("category-input").value)) {
                is ShopResult.Success -> {
                    input("name-input").value = ""
                    input("quantity-input").value = "1"
                    input("category-input").value = ""
                    render()
                }
                is ShopResult.Failure -> element("error").textContent = result.message
            }
        }
    })
    element("clear-btn").addEventListener("click", {
        scope.launch {
            try {
                clearChecked()
                render()
            } catch (t: Throwable) {
                element("error").textContent = "Could not reach the server at http://localhost:8080"
            }
        }
    })
    scope.launch { render() }
}

private suspend fun render() {
    val visible: List<ShoppingItem>
    val categories: List<String>
    try {
        visible = listItems(selectedCategory)
        categories = listItems().map { it.category }.filter { it.isNotEmpty() }.distinct().sorted()
    } catch (t: Throwable) {
        element("error").textContent = "Could not reach the server at http://localhost:8080 — is ./gradlew :server:run running?"
        return
    }
    element("error").textContent = ""
    element("clear-btn").style.display = if (visible.any { it.checked }) "inline-block" else "none"
    fill(element("category-filter"), (listOf("") + categories).map { categoryChip(it) })
    fill(element("item-list"), visible.map { itemRow(it) })
}

private fun fill(parent: HTMLElement, children: List<HTMLElement>) {
    while (parent.firstChild != null) parent.removeChild(parent.firstChild!!)
    children.forEach { parent.appendChild(it) }
}

private fun categoryChip(category: String): HTMLElement =
    button(category.ifEmpty { "All" }, if (category == selectedCategory) "chip active" else "chip") {
        selectedCategory = category
        render()
    }

private fun itemRow(item: ShoppingItem): HTMLElement {
    val li = document.createElement("li") as HTMLElement
    li.className = if (item.checked) "done" else ""
    li.appendChild(button(if (item.checked) "☑" else "☐", "check-btn") { report(toggleChecked(item.id)) })
    val text = div("text", "")
    text.appendChild(div("name", "${item.name} × ${item.quantity}"))
    text.appendChild(div("category", item.category.ifEmpty { "uncategorised" }))
    li.appendChild(text)
    li.appendChild(button("Delete", "delete-btn") { report(removeItem(item.id)) })
    return li
}

private suspend fun report(result: ShopResult<*>) {
    when (result) {
        is ShopResult.Success -> render()
        is ShopResult.Failure -> element("error").textContent = result.message
    }
}

private fun div(className: String, text: String): HTMLElement {
    val node = document.createElement("div") as HTMLElement
    node.className = className
    node.textContent = text
    return node
}

private fun button(label: String, className: String, onClick: suspend () -> Unit): HTMLElement {
    val node = document.createElement("button") as HTMLElement
    node.className = className
    node.textContent = label
    node.addEventListener("click", { scope.launch { onClick() } })
    return node
}
