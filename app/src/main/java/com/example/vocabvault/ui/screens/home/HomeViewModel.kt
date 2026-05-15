package com.example.vocabvault.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabvault.data.local.WordEntity
import com.example.vocabvault.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Tab index constants for clarity. */
object HomeTabs {
    const val ALL = 0
    const val STARRED = 1
    const val THIS_WEEK = 2
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WordRepository
) : ViewModel() {

    /** Currently selected tab (0 = All, 1 = Starred, 2 = This Week). */
    private val _selectedTab = MutableStateFlow(HomeTabs.ALL)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    /** Set of currently selected word IDs for bulk operations. */
    private val _selectedWordIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedWordIds: StateFlow<Set<Int>> = _selectedWordIds.asStateFlow()

    /** Whether bulk delete mode is active. */
    private val _isBulkDeleteMode = MutableStateFlow(false)
    val isBulkDeleteMode: StateFlow<Boolean> = _isBulkDeleteMode.asStateFlow()

    /**
     * Word list that automatically switches its upstream Flow when the tab changes.
     * [flatMapLatest] cancels the previous Flow before subscribing to the new one.
     */
    val words: StateFlow<List<WordEntity>> = _selectedTab
        .flatMapLatest { tab ->
            when (tab) {
                HomeTabs.STARRED -> repository.getStarredWords()
                HomeTabs.THIS_WEEK -> repository.getWordsThisWeek()
                else -> repository.getAllWords()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun toggleStar(entity: WordEntity) {
        viewModelScope.launch {
            repository.toggleStar(entity)
        }
    }

    // ── Bulk delete operations ─────────────────────────────────────────────────

    /** Toggle bulk delete mode. */
    fun toggleBulkDeleteMode() {
        _isBulkDeleteMode.value = !_isBulkDeleteMode.value
        if (!_isBulkDeleteMode.value) {
            // Clear selections when exiting bulk mode
            _selectedWordIds.value = emptySet()
        }
    }

    /** Toggle selection of a word. */
    fun toggleWordSelection(wordId: Int) {
        val current = _selectedWordIds.value.toMutableSet()
        if (current.contains(wordId)) {
            current.remove(wordId)
        } else {
            current.add(wordId)
        }
        _selectedWordIds.value = current.toSet()
    }

    /** Select all words currently shown. */
    fun selectAllShown(words: List<WordEntity>) {
        _selectedWordIds.value = words.map { it.id }.toSet()
    }

    /** Delete all selected words. */
    fun deleteSelected() {
        viewModelScope.launch {
            val ids = _selectedWordIds.value.toList()
            if (ids.isNotEmpty()) {
                repository.deleteMultipleWords(ids)
                _selectedWordIds.value = emptySet()
                _isBulkDeleteMode.value = false
            }
        }
    }

    /** Clear all selections. */
    fun clearSelection() {
        _selectedWordIds.value = emptySet()
    }
}
