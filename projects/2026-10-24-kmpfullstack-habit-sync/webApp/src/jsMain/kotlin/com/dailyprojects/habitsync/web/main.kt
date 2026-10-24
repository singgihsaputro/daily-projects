package com.dailyprojects.habitsync.web

import com.dailyprojects.habitsync.AddHabitUseCase
import com.dailyprojects.habitsync.HabitResult
import com.dailyprojects.habitsync.HabitView
import com.dailyprojects.habitsync.ListHabitsUseCase
import com.dailyprojects.habitsync.RemoveHabitUseCase
import com.dailyprojects.habitsync.ToggleCheckInUseCase
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event

private const val SERVER = "http://localhost:8080"

private val repository = HttpHabitRepository()
private val listHabits = ListHabitsUseCase(repository)
private val addHabit = AddHabitUseCase(repository)
private val toggleCheckIn = ToggleCheckInUseCase(repository)
private val removeHabit = RemoveHabitUseCase(repository)
private val scope = MainScope()

private fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
private fun element(id: String) = document.getElementById(id) as HTMLElement
private fun input(id: String) = document.getElementById(id) as HTMLInputElement

fun main() {
    (document.getElementById("create-form") as HTMLFormElement).addEventListener("submit", { event: Event ->
        event.preventDefault()
        scope.launch {
            when (val result = addHabit(input("name-input").value, input("goal-input").value.toIntOrNull() ?: 0)) {
                is HabitResult.Success -> {
                    input("name-input").value = ""
                    input("goal-input").value = "7"
                    render()
                }
                is HabitResult.Failure -> element("error").textContent = result.message
            }
        }
    })
    scope.launch { render() }
}

private suspend fun render() {
    val habits = try {
        listHabits(today())
    } catch (t: Throwable) {
        element("error").textContent = "Could not reach the server at $SERVER — is ./gradlew :server:run running?"
        return
    }
    element("error").textContent = ""
    val list = element("habit-list")
    while (list.firstChild != null) list.removeChild(list.firstChild!!)
    habits.forEach { list.appendChild(habitCard(it)) }
}

private fun habitCard(view: HabitView): HTMLElement {
    val card = node("li", "card")
    val head = node("div", "head")
    head.appendChild(node("div", "name", view.habit.name))
    head.appendChild(node("div", if (view.goalMet) "badge met" else "badge", "${view.streak}-day streak"))
    card.appendChild(head)

    val week = node("div", "week")
    val todayDate = today()
    for (back in 6 downTo 0) {
        val date = todayDate.minus(DatePeriod(days = back))
        val done = date.toString() in view.habit.checkIns
        val dot = node("button", if (done) "dot done" else "dot", date.dayOfWeek.name.take(1))
        dot.setAttribute("title", date.toString())
        dot.addEventListener("click", { scope.launch { report(toggleCheckIn(view.habit.id, date.toString(), todayDate)) } })
        week.appendChild(dot)
    }
    card.appendChild(week)

    val foot = node("div", "foot")
    foot.appendChild(node("span", "progress", "${view.doneThisWeek} / ${view.habit.goalPerWeek} this week"))
    val delete = node("button", "delete-btn", "Delete")
    delete.addEventListener("click", { scope.launch { report(removeHabit(view.habit.id)) } })
    foot.appendChild(delete)
    card.appendChild(foot)
    return card
}

private suspend fun report(result: HabitResult<*>) {
    when (result) {
        is HabitResult.Success -> render()
        is HabitResult.Failure -> element("error").textContent = result.message
    }
}

private fun node(tag: String, className: String, text: String = ""): HTMLElement {
    val node = document.createElement(tag) as HTMLElement
    node.className = className
    node.textContent = text
    return node
}
