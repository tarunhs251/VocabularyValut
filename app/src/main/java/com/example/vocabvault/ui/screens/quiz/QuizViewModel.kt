package com.example.vocabvault.ui.screens.quiz

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabvault.data.local.WordEntity
import com.example.vocabvault.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random
/** Direction of a quiz card. */
enum class QuizMode {
    /** Show meaning → user guesses the word. */
    MEANING_TO_WORD,
    /** Show word → user guesses the meaning. */
    WORD_TO_MEANING
}

data class QuizOption(
    val id: String,
    val text: String,
    val isCorrect: Boolean
)

data class QuizCard(
    val entity: WordEntity,
    val mode: QuizMode,
    val options: List<QuizOption> = emptyList(),
    val selectedOptionId: String? = null,
    val isAnswerRevealed: Boolean = false
) {
    val isCorrectSelected: Boolean
        get() = selectedOptionId != null && options.find { it.id == selectedOptionId }?.isCorrect == true
}

data class QuizScore(
    val correct: Int = 0,
    val total: Int = 0
) {
    val accuracyPercent: Int
        get() = if (total == 0) 0 else (correct * 100 / total)
}

sealed class QuizUiState {
    object Idle : QuizUiState()
    object Loading : QuizUiState()
    data class Active(val card: QuizCard, val score: QuizScore, val remaining: Int) : QuizUiState()
    data class Finished(val score: QuizScore) : QuizUiState()
    data class Error(val message: String) : QuizUiState()
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: WordRepository
) : ViewModel() {
    companion object {
        private const val TAG = "QuizViewModel"
    }

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Idle)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // Internal queue of cards
    private val queue = ArrayDeque<QuizCard>()
    private var score = QuizScore()
    private var allWords: List<WordEntity> = emptyList()

    // ── Public actions ────────────────────────────────────────────────────────

    /** Load [count] random words and start the MCQ quiz. */
    fun startQuiz(count: Int = 20) {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            queue.clear()
            score = QuizScore()

            allWords = repository.getRandomWordsForQuiz(count)
            if (allWords.isEmpty()) {
                _uiState.value = QuizUiState.Error("Add at least one word to start a quiz.")
                return@launch
            }

            // Build shuffled queue with MCQ options (with async API calls)
            allWords.shuffled().forEach { entity ->
                val mode = if (Random.nextDouble() < 0.5) QuizMode.MEANING_TO_WORD else QuizMode.WORD_TO_MEANING
                val options = generateMcqOptions(entity, mode, allWords)
                queue.addLast(QuizCard(entity = entity, mode = mode, options = options))
            }

            advanceToNextCard()
        }
    }

    /** Select an answer option. */
    fun selectOption(optionId: String) {
        val current = currentActive() ?: return
        _uiState.value = QuizUiState.Active(
            card = current.card.copy(
                selectedOptionId = optionId,
                isAnswerRevealed = true
            ),
            score = current.score,
            remaining = current.remaining
        )
    }

    /** Move to next card (after option is selected and revealed). */
    fun nextQuestion() {
        val current = currentActive() ?: return
        val isCorrect = current.card.isCorrectSelected
        
        score = score.copy(
            correct = if (isCorrect) score.correct + 1 else score.correct,
            total = score.total + 1
        )
        
        viewModelScope.launch {
            if (isCorrect) {
                repository.incrementCorrect(current.card.entity)
            } else {
                repository.incrementWrong(current.card.entity)
            }
            advanceToNextCard()
        }
    }

    fun resetQuiz() {
        queue.clear()
        score = QuizScore()
        allWords = emptyList()
        _uiState.value = QuizUiState.Idle
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Generate 4 MCQ options with smart distractors using API synonyms/antonyms when available.
     * Falls back to random words if API is unavailable.
     */
    private suspend fun generateMcqOptions(
        correctEntity: WordEntity,
        mode: QuizMode,
        allWords: List<WordEntity>
    ): List<QuizOption> {
        // Correct answer
        val correctText = if (mode == QuizMode.MEANING_TO_WORD) {
            correctEntity.word
        } else {
            correctEntity.meaning
        }
        
        val correctOption = QuizOption(
            id = "correct_${correctEntity.id}",
            text = correctText,
            isCorrect = true
        )

        // Try to get smart distractors from API first
        val distractors = try {
            val apiDistracts = repository.getSmartDistracsFromApi(
                word = correctEntity.word,
                mode = mode,
                count = 3
            )
            
            if (apiDistracts.isNotEmpty()) {
                Log.d(TAG, "Using API-based distractors for: ${correctEntity.word}")
                apiDistracts.map { text ->
                    QuizOption(
                        id = "api_distract_${correctEntity.id}_${text.hashCode()}",
                        text = text,
                        isCorrect = false
                    )
                }
            } else {
                // Fallback to random words
                getRandomDistracts(correctEntity, mode, allWords, 3)
            }
        } catch (e: Exception) {
            Log.w(TAG, "API distract fetch failed, using random fallback", e)
            // Fallback to random words
            getRandomDistracts(correctEntity, mode, allWords, 3)
        }

        // Ensure we have exactly 3 distractors
        val finalDistracts = if (distractors.size < 3) {
            val randomDistracts = getRandomDistracts(correctEntity, mode, allWords, 3 - distractors.size)
            distractors + randomDistracts
        } else {
            distractors.take(3)
        }

        // Combine and shuffle
        return (listOf(correctOption) + finalDistracts).shuffled()
    }

    /**
     * Get random distractors from existing vocabulary.
     */
    private fun getRandomDistracts(
        correctEntity: WordEntity,
        mode: QuizMode,
        allWords: List<WordEntity>,
        count: Int
    ): List<QuizOption> {
        return allWords
            .filter { it.id != correctEntity.id }
            .shuffled()
            .take(count)
            .mapIndexed { index, entity ->
                val distractorText = if (mode == QuizMode.MEANING_TO_WORD) {
                    entity.word
                } else {
                    entity.meaning
                }
                QuizOption(
                    id = "distractor_${entity.id}_$index",
                    text = distractorText,
                    isCorrect = false
                )
            }
    }

    private fun advanceToNextCard() {
        if (queue.isEmpty()) {
            _uiState.value = QuizUiState.Finished(score)
        } else {
            val nextCard = queue.removeFirst()
            _uiState.value = QuizUiState.Active(
                card = nextCard,
                score = score,
                remaining = queue.size
            )
        }
    }

    private fun currentActive(): QuizUiState.Active? =
        _uiState.value as? QuizUiState.Active
}
