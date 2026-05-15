package com.example.vocabvault.ui.screens.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabvault.data.local.WordEntity
import com.example.vocabvault.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    private val repository: WordRepository
) : ViewModel() {

    private val _word = MutableStateFlow<WordEntity?>(null)
    val word: StateFlow<WordEntity?> = _word.asStateFlow()

    /** Triggers navigation back to the list after deletion. */
    var deleted by mutableStateOf(false)
        private set

    fun loadWord(id: Int) {
        viewModelScope.launch {
            _word.value = repository.getWordById(id)
        }
    }

    fun toggleStar(entity: WordEntity) {
        viewModelScope.launch {
            repository.toggleStar(entity)
            // Reload to reflect updated state
            _word.value = repository.getWordById(entity.id)
        }
    }

    fun deleteWord(entity: WordEntity) {
        viewModelScope.launch {
            repository.deleteWord(entity)
            deleted = true
        }
    }
}
