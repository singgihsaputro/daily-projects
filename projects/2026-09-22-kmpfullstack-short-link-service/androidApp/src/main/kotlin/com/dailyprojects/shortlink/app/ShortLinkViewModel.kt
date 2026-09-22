package com.dailyprojects.shortlink.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyprojects.shortlink.CreateShortLinkUseCase
import com.dailyprojects.shortlink.LinkRepository
import com.dailyprojects.shortlink.LinkResult
import com.dailyprojects.shortlink.ListLinksUseCase
import com.dailyprojects.shortlink.ShortLink
import com.dailyprojects.shortlink.VisitLinkUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ShortLinkUiState(
    val links: List<ShortLink> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val newUrlInput: String = "",
)

class ShortLinkViewModel(repository: LinkRepository = HttpLinkRepository()) : ViewModel() {
    private val listLinks = ListLinksUseCase(repository)
    private val createLink = CreateShortLinkUseCase(repository)
    private val visitLink = VisitLinkUseCase(repository)

    private val _state = MutableStateFlow(ShortLinkUiState())
    val state: StateFlow<ShortLinkUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun onNewUrlChange(value: String) {
        _state.value = _state.value.copy(newUrlInput = value)
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            runCatching { listLinks() }
                .onSuccess { links -> _state.value = _state.value.copy(links = links, isLoading = false, errorMessage = null) }
                .onFailure { error -> _state.value = _state.value.copy(isLoading = false, errorMessage = error.message) }
        }
    }

    fun addLink() {
        val url = _state.value.newUrlInput
        viewModelScope.launch {
            when (val result = createLink(url)) {
                is LinkResult.Success -> {
                    _state.value = _state.value.copy(newUrlInput = "", errorMessage = null)
                    refresh()
                }
                is LinkResult.Failure -> _state.value = _state.value.copy(errorMessage = result.message)
            }
        }
    }

    fun visit(code: String) {
        viewModelScope.launch {
            when (val result = visitLink(code)) {
                is LinkResult.Success -> refresh()
                is LinkResult.Failure -> _state.value = _state.value.copy(errorMessage = result.message)
            }
        }
    }
}
