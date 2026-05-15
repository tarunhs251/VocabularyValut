package com.example.vocabvault.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a single vocabulary word.
 * The [word] field has a unique index to prevent duplicate entries.
 */
@Entity(
    tableName = "words",
    indices = [Index(value = ["word"], unique = true)]
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** The vocabulary word (normalized to lowercase on insert). */
    val word: String,

    /** Primary definition fetched from the dictionary API or entered manually. */
    val meaning: String,

    /** Example sentence, if available. Empty string when not found. */
    val example: String = "",

    /** Part of speech (noun, verb, adjective, etc.). */
    val partOfSpeech: String = "",

    /** Whether the user has starred/bookmarked this word. */
    val isStarred: Boolean = false,

    /** Epoch milliseconds when the word was added. */
    val dateAdded: Long = System.currentTimeMillis(),

    /** Number of times the user answered correctly in quiz mode. */
    val correctCount: Int = 0,

    /** Number of times the user answered incorrectly in quiz mode. */
    val wrongCount: Int = 0,

    /** Comma-separated list of synonyms fetched from the API. */
    val synonyms: String = ""
)
