package com.example.vocabvault.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [WordEntity].
 * All list queries return [Flow] so the UI reacts automatically to DB changes.
 */
@Dao
interface WordDao {

    // ── Queries ──────────────────────────────────────────────────────────────

    /** All words ordered newest-first. */
    @Query("SELECT * FROM words ORDER BY dateAdded DESC")
    fun getAllWords(): Flow<List<WordEntity>>

    /** Words the user has starred, newest-first. */
    @Query("SELECT * FROM words WHERE isStarred = 1 ORDER BY dateAdded DESC")
    fun getStarredWords(): Flow<List<WordEntity>>

    /**
     * Words added on or after [timestamp] (epoch ms).
     * Use DateUtils.weekAgoMillis() to get "this week" threshold.
     */
    @Query("SELECT * FROM words WHERE dateAdded >= :timestamp ORDER BY dateAdded DESC")
    fun getWordsAddedAfter(timestamp: Long): Flow<List<WordEntity>>

    /**
     * Case-insensitive search across [word] and [meaning].
     * Pass the query wrapped in % wildcards: "%term%".
     */
    @Query("SELECT * FROM words WHERE word LIKE :query OR meaning LIKE :query ORDER BY dateAdded DESC")
    fun searchWords(query: String): Flow<List<WordEntity>>

    /** Fetch a fixed number of random words for quiz mode. Suspend (one-shot). */
    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomWords(limit: Int): List<WordEntity>

    /** Lookup by exact word string (normalized/lowercased). Suspend (one-shot). */
    @Query("SELECT * FROM words WHERE word = :word LIMIT 1")
    suspend fun getWordByName(word: String): WordEntity?

    /** Lookup by primary key. */
    @Query("SELECT * FROM words WHERE id = :id LIMIT 1")
    suspend fun getWordById(id: Int): WordEntity?

    // ── Stats helpers ─────────────────────────────────────────────────────────

    @Query("SELECT COUNT(*) FROM words")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE isStarred = 1")
    fun getStarredCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE dateAdded >= :timestamp")
    fun getCountAddedAfter(timestamp: Long): Flow<Int>

    @Query("SELECT SUM(correctCount) FROM words")
    fun getTotalCorrect(): Flow<Int?>

    @Query("SELECT SUM(wrongCount) FROM words")
    fun getTotalWrong(): Flow<Int?>

    // ── Mutations ─────────────────────────────────────────────────────────────

    /**
     * Insert a word. Returns the new row ID, or -1 if a duplicate was ignored
     * (due to the UNIQUE index on [WordEntity.word]).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: WordEntity): Long

    /** Replace-update a full entity (used for star toggle, quiz count updates). */
    @Update
    suspend fun update(word: WordEntity)

    @Delete
    suspend fun delete(word: WordEntity)

    /**
     * Delete multiple words by ID.
     */
    @Query("DELETE FROM words WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    /**
     * Delete all starred words.
     */
    @Query("DELETE FROM words WHERE isStarred = 1")
    suspend fun deleteAllStarred()
}
