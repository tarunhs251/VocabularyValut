package com.example.vocabvault.ui.screens.addword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabvault.data.local.WordEntity
import com.example.vocabvault.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/** All possible states of the Add Word screen. */
sealed class AddWordUiState {
    object Idle : AddWordUiState()
    object Loading : AddWordUiState()
    /** API returned a draft — user can now edit fields before saving. */
    data class DefinitionReady(val entity: WordEntity) : AddWordUiState()
    data class Error(val message: String) : AddWordUiState()
    /** Successfully saved to Room. */
    object Saved : AddWordUiState()
    /** The word already exists in the local DB. */
    data class Duplicate(val word: String) : AddWordUiState()
}

@HiltViewModel
class AddWordViewModel @Inject constructor(
    private val repository: WordRepository
) : ViewModel() {

    // ── Input fields ──────────────────────────────────────────────────────────

    private val _wordInput = MutableStateFlow("")
    val wordInput: StateFlow<String> = _wordInput.asStateFlow()

    private val _meaningInput = MutableStateFlow("")
    val meaningInput: StateFlow<String> = _meaningInput.asStateFlow()

    private val _exampleInput = MutableStateFlow("")
    val exampleInput: StateFlow<String> = _exampleInput.asStateFlow()

    private val _partOfSpeechInput = MutableStateFlow("")
    val partOfSpeechInput: StateFlow<String> = _partOfSpeechInput.asStateFlow()

    private val _synonymsInput = MutableStateFlow("")
    val synonymsInput: StateFlow<String> = _synonymsInput.asStateFlow()

    // ── UI state ──────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow<AddWordUiState>(AddWordUiState.Idle)
    val uiState: StateFlow<AddWordUiState> = _uiState.asStateFlow()

    // ── Input updates ─────────────────────────────────────────────────────────

    fun onWordInputChange(value: String) {
        _wordInput.value = value
        // Reset state so previous error/success doesn't linger after editing
        if (_uiState.value !is AddWordUiState.Idle) {
            _uiState.value = AddWordUiState.Idle
        }
    }

    fun onMeaningInputChange(value: String) { _meaningInput.value = value }
    fun onExampleInputChange(value: String) { _exampleInput.value = value }
    fun onSynonymsInputChange(value: String) { _synonymsInput.value = value }

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Fetch definition from API (or return cached DB entry).
     * Pre-fills meaning/example fields on success so the user can review/edit.
     */
    fun fetchDefinition() {
        val word = _wordInput.value.trim()
        if (word.isBlank()) {
            _uiState.value = AddWordUiState.Error("Please enter a word first.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddWordUiState.Loading

            repository.fetchDefinition(word).fold(
                onSuccess = { entity ->
                    if (entity.id != 0) {
                        // Already saved in DB
                        _uiState.value = AddWordUiState.Duplicate(entity.word)
                    } else {
                        _meaningInput.value = entity.meaning
                        _exampleInput.value = entity.example
                        _partOfSpeechInput.value = entity.partOfSpeech
                        _synonymsInput.value = entity.synonyms
                        _wordInput.value = entity.word // normalized form
                        _uiState.value = AddWordUiState.DefinitionReady(entity)
                    }
                },
                onFailure = { error ->
                    // API failed — user can still fill fields manually
                    _uiState.value = AddWordUiState.Error(
                        error.message ?: "Could not fetch definition. You can add it manually."
                    )
                }
            )
        }
    }

    /**
     * Save the current field values to Room.
     * Validates meaning is non-empty before saving.
     */
    fun saveWord() {
        val word = _wordInput.value.trim().lowercase()
        val meaning = _meaningInput.value.trim()

        if (word.isBlank()) {
            _uiState.value = AddWordUiState.Error("Word cannot be empty.")
            return
        }
        if (meaning.isBlank()) {
            _uiState.value = AddWordUiState.Error("Please provide a meaning before saving.")
            return
        }

        val entity = WordEntity(
            word = word,
            meaning = meaning,
            example = _exampleInput.value.trim(),
            partOfSpeech = _partOfSpeechInput.value.trim(),
            synonyms = _synonymsInput.value.trim()
        )

        viewModelScope.launch {
            _uiState.value = AddWordUiState.Loading
            repository.saveWord(entity).fold(
                onSuccess = { _uiState.value = AddWordUiState.Saved },
                onFailure = { error ->
                    if (error.message?.contains("already in your vocabulary") == true) {
                        _uiState.value = AddWordUiState.Duplicate(word)
                    } else {
                        _uiState.value = AddWordUiState.Error(
                            error.message ?: "Could not save word."
                        )
                    }
                }
            )
        }
    }

    /** Clears all fields and resets state (called when screen is closed or after save). */
    fun reset() {
        _wordInput.value = ""
        _meaningInput.value = ""
        _exampleInput.value = ""
        _partOfSpeechInput.value = ""
        _synonymsInput.value = ""
        _uiState.value = AddWordUiState.Idle
    }
}
