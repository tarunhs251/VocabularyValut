package com.example.vocabvault.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Top-level entry returned by dictionaryapi.dev.
 * The API returns a JSON array; we only need the first element.
 *
 * Example endpoint: GET https://api.dictionaryapi.dev/api/v2/entries/en/{word}
 */
data class DictionaryEntry(
    @SerializedName("word")
    val word: String,
    @SerializedName("phonetic")
    val phonetic: String? = null,
    @SerializedName("phonetics")
    val phonetics: List<Phonetic> = emptyList(),
    @SerializedName("meanings")
    val meanings: List<Meaning> = emptyList(),
    @SerializedName("license")
    val license: License? = null,
    @SerializedName("sourceUrls")
    val sourceUrls: List<String> = emptyList()
)

data class Phonetic(
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("audio")
    val audio: String? = null
)

data class Meaning(
    @SerializedName("partOfSpeech")
    val partOfSpeech: String = "",
    @SerializedName("definitions")
    val definitions: List<Definition> = emptyList(),
    @SerializedName("synonyms")
    val synonyms: List<String> = emptyList(),
    @SerializedName("antonyms")
    val antonyms: List<String> = emptyList()
)

data class Definition(
    @SerializedName("definition")
    val definition: String = "",
    @SerializedName("example")
    val example: String? = null,
    @SerializedName("synonyms")
    val synonyms: List<String> = emptyList(),
    @SerializedName("antonyms")
    val antonyms: List<String> = emptyList()
)

data class License(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("url")
    val url: String = ""
)

/**
 * Error response shape returned when the word is not found:
 * { "title": "No Definitions Found", "message": "...", "resolution": "..." }
 */
data class DictionaryErrorResponse(
    val title: String,
    val message: String,
    val resolution: String
)

// ── Extension helpers ──────────────────────────────────────────────────────

/**
 * Extracts the primary meaning (first definition of the first meaning entry).
 * Returns an empty string when no meanings are present.
 */
fun DictionaryEntry.primaryMeaning(): String =
    meanings.firstOrNull()?.definitions?.firstOrNull()?.definition.orEmpty()

/**
 * Extracts all unique synonyms from all meanings.
 * Returns a comma-separated string of synonyms.
 */
fun DictionaryEntry.extractSynonyms(): String =
    meanings.flatMap { it.synonyms }
        .filterNot { it.isBlank() }
        .distinct()
        .joinToString(", ")

/**
 * Scans all meanings and definitions for the first non-null example sentence.
 */
fun DictionaryEntry.firstExample(): String =
    meanings.flatMap { it.definitions }
        .firstOrNull { !it.example.isNullOrBlank() }
        ?.example
        .orEmpty()

/** Returns the part of speech of the first meaning entry. */
fun DictionaryEntry.primaryPartOfSpeech(): String =
    meanings.firstOrNull()?.partOfSpeech.orEmpty()
