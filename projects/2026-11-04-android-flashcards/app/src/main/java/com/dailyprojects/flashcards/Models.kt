package com.dailyprojects.flashcards

data class Card(val id: String, val front: String, val back: String)

data class Deck(val name: String, val cards: List<Card>)

interface DeckRepository {
    suspend fun load(): Deck
}

data class StudyState(
    val deckName: String = "",
    val queue: List<Card> = emptyList(),
    val total: Int = 0,
    val known: Int = 0,
    val revealed: Boolean = false,
    val loading: Boolean = true,
) {
    val current: Card? get() = queue.firstOrNull()
    val finished: Boolean get() = !loading && queue.isEmpty()
}
