package com.example.vocabvault.data.repository

import android.util.Log
import com.example.vocabvault.data.local.WordDao
import com.example.vocabvault.data.local.WordEntity
import com.example.vocabvault.data.remote.DictionaryApiService
import com.example.vocabvault.data.remote.model.firstExample
import com.example.vocabvault.data.remote.model.primaryMeaning
import com.example.vocabvault.data.remote.model.primaryPartOfSpeech
import com.example.vocabvault.data.remote.model.extractSynonyms
import com.example.vocabvault.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for all word data.
 *
 * - Room is always the primary store; the app works fully offline.
 * - The API is called only when adding a new word (to pre-fill the definition).
 * - All DB mutations run on [Dispatchers.IO].
 */
@Singleton
class WordRepository @Inject constructor(
    private val wordDao: WordDao,
    private val apiService: DictionaryApiService
) {
    companion object {
        private const val TAG = "WordRepository"
    }

    // ── Observe (Flows for live UI) ───────────────────────────────────────────

    fun getAllWords(): Flow<List<WordEntity>> = wordDao.getAllWords()

    fun getStarredWords(): Flow<List<WordEntity>> = wordDao.getStarredWords()

    fun getWordsThisWeek(): Flow<List<WordEntity>> =
        wordDao.getWordsAddedAfter(DateUtils.weekAgoMillis())

    /** [query] should be the raw search term — wrapping in % is done here. */
    fun searchWords(query: String): Flow<List<WordEntity>> {
        val sanitized = query.trim()
        if (sanitized.isBlank()) return wordDao.getAllWords()
        // Safe: parameterized LIKE query, no string concatenation in SQL
        return wordDao.searchWords("%$sanitized%")
    }

    fun getTotalCount(): Flow<Int> = wordDao.getTotalCount()
    fun getStarredCount(): Flow<Int> = wordDao.getStarredCount()
    fun getCountThisWeek(): Flow<Int> = wordDao.getCountAddedAfter(DateUtils.weekAgoMillis())
    fun getTotalCorrect(): Flow<Int?> = wordDao.getTotalCorrect()
    fun getTotalWrong(): Flow<Int?> = wordDao.getTotalWrong()

    // ── One-shot queries ──────────────────────────────────────────────────────

    suspend fun getWordById(id: Int): WordEntity? = withContext(Dispatchers.IO) {
        wordDao.getWordById(id)
    }

    suspend fun getRandomWordsForQuiz(count: Int = 20): List<WordEntity> =
        withContext(Dispatchers.IO) { wordDao.getRandomWords(count) }

    // ── API + Save ────────────────────────────────────────────────────────────

    /**
     * Attempts to fetch a definition from the dictionary API.
     *
     * Behaviour:
     * 1. Normalizes [rawWord] (trim + lowercase).
     * 2. Checks Room — if already saved, returns [Result.success] with the cached entity.
     * 3. Calls API — on success, returns a *draft* [WordEntity] (NOT yet persisted).
     *    The caller (ViewModel) shows it for review before calling [saveWord].
     * 4. On any network/HTTP error, returns [Result.failure] with a user-friendly message.
     *
     * @return [Result.success] with a draft [WordEntity] (id = 0) or cached entity.
     */
    suspend fun fetchDefinition(rawWord: String): Result<WordEntity> =
        withContext(Dispatchers.IO) {
            val normalized = rawWord.trim().lowercase()
            if (normalized.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Word cannot be empty"))
            }

            Log.d(TAG, "Attempting to fetch definition for: $normalized")

            // Return cached copy immediately if already in DB
            val existing = wordDao.getWordByName(normalized)
            if (existing != null) {
                Log.d(TAG, "Found cached definition for: $normalized")
                return@withContext Result.success(existing)
            }

            // Network call
            return@withContext try {
                Log.d(TAG, "Making API call to dictionary service for: $normalized")
                val entries = apiService.getDefinition(normalized)
                Log.d(TAG, "API call successful. Received ${entries.size} entries for: $normalized")
                
                val entry = entries.firstOrNull()
                    ?: run {
                        Log.w(TAG, "No definition found for: $normalized")
                        return@withContext Result.failure(Exception("No definition found for \"$normalized\""))
                    }

                val draft = WordEntity(
                    word = normalized,
                    meaning = entry.primaryMeaning(),
                    example = entry.firstExample(),
                    partOfSpeech = entry.primaryPartOfSpeech(),
                    synonyms = entry.extractSynonyms()
                )
                Log.d(TAG, "Successfully parsed definition for: $normalized")
                Result.success(draft)
            } catch (e: retrofit2.HttpException) {
                Log.e(TAG, "HTTP Error: ${e.code()} for word: $normalized", e)
                val msg = when (e.code()) {
                    404 -> "\"$normalized\" was not found in the dictionary."
                    429 -> "Too many requests. Please wait a moment."
                    else -> "Server error (${e.code()}). Try again later."
                }
                Result.failure(Exception(msg))
            } catch (e: java.io.IOException) {
                Log.e(TAG, "Network error (IOException) for word: $normalized", e)
                Result.failure(Exception("No internet connection. You can still add the word manually."))
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error for word: $normalized", e)
                Result.failure(Exception("Unexpected error: ${e.message}"))
            }
        }

    /**
     * Persists [entity] to Room.
     *
     * @return [Result.success] with the saved entity on success,
     *         [Result.failure] with "duplicate" error if the word already exists.
     */
    suspend fun saveWord(entity: WordEntity): Result<WordEntity> =
        withContext(Dispatchers.IO) {
            val insertedId = wordDao.insert(entity)
            if (insertedId == -1L) {
                // UNIQUE constraint triggered — duplicate
                Result.failure(Exception("\"${entity.word}\" is already in your vocabulary."))
            } else {
                Result.success(entity.copy(id = insertedId.toInt()))
            }
        }

    // ── Star toggle ───────────────────────────────────────────────────────────

    suspend fun toggleStar(entity: WordEntity) = withContext(Dispatchers.IO) {
        wordDao.update(entity.copy(isStarred = !entity.isStarred))
    }

    // ── Quiz mutations ────────────────────────────────────────────────────────

    suspend fun incrementCorrect(entity: WordEntity) = withContext(Dispatchers.IO) {
        wordDao.update(entity.copy(correctCount = entity.correctCount + 1))
    }

    suspend fun incrementWrong(entity: WordEntity) = withContext(Dispatchers.IO) {
        wordDao.update(entity.copy(wrongCount = entity.wrongCount + 1))
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    suspend fun deleteWord(entity: WordEntity) = withContext(Dispatchers.IO) {
        wordDao.delete(entity)
    }

    /** Update an existing entity (e.g., after user edits meaning/example in detail screen). */
    suspend fun updateWord(entity: WordEntity) = withContext(Dispatchers.IO) {
        wordDao.update(entity)
    }

    // ── Quiz helpers (Smart MCQ distractors) ───────────────────────────────────

    /**
     * Get smart distractors from the API (synonyms/antonyms).
     * Falls back to empty list if API fails.
     */
    suspend fun getSmartDistracsFromApi(
        word: String,
        mode: com.example.vocabvault.ui.screens.quiz.QuizMode,
        count: Int = 3
    ): List<String> = withContext(Dispatchers.IO) {
        try {
            val entries = apiService.getDefinition(word)
            val entry = entries.firstOrNull() ?: return@withContext emptyList()

            // Collect all synonyms and antonyms
            val allSynonymsAntonyms = entry.meanings.flatMap { meaning ->
                (meaning.synonyms + meaning.antonyms).distinct()
            }.filterNot { it.isBlank() }

            if (allSynonymsAntonyms.isEmpty()) {
                return@withContext emptyList()
            }

            // For WORD_TO_MEANING, we want synonyms as distractors
            // For MEANING_TO_WORD, we want words that have similar meanings (antonyms/opposite)
            allSynonymsAntonyms.shuffled().take(count)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch smart distractors from API", e)
            emptyList()
        }
    }

    // ── Bulk operations ────────────────────────────────────────────────────────

    /**
     * Delete multiple words by their IDs.
     */
    suspend fun deleteMultipleWords(ids: List<Int>) = withContext(Dispatchers.IO) {
        wordDao.deleteByIds(ids)
    }

    /**
     * Delete all starred words.
     */
    suspend fun deleteAllStarred() = withContext(Dispatchers.IO) {
        wordDao.deleteAllStarred()
    }
}
