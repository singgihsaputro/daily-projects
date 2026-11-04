package com.dailyprojects.flashcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StudyViewModel(private val repository: DeckRepository) : ViewModel() {
    private val _state = MutableStateFlow(StudyState())
    val state: StateFlow<StudyState> = _state.asStateFlow()

    init {
        restart()
    }

    fun restart() {
        viewModelScope.launch {
            _state.value = StudyState(loading = true)
            val deck = repository.load()
            _state.value = StudyState(
                deckName = deck.name,
                queue = deck.cards.shuffled(),
                total = deck.cards.size,
                loading = false,
            )
        }
    }

    fun flip() = _state.update { it.copy(revealed = !it.revealed) }

    /** Known cards leave the queue; missed cards go to the back so they come around again. */
    fun answer(knewIt: Boolean) = _state.update { s ->
        val card = s.current ?: return@update s
        val rest = s.queue.drop(1)
        s.copy(
            queue = if (knewIt) rest else rest + card,
            known = if (knewIt) s.known + 1 else s.known,
            revealed = false,
        )
    }
}
