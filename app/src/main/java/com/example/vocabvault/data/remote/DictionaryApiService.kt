package com.example.vocabvault.data.remote

import com.example.vocabvault.data.remote.model.DictionaryEntry
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit service for the Free Dictionary API.
 * Base URL: https://api.dictionaryapi.dev/
 *
 * The API is free, requires no key, and returns an array of [DictionaryEntry].
 * HTTP 404 is returned when a word is not found — Retrofit throws an [HttpException]
 * which the repository catches and converts to [Result.failure].
 */
interface DictionaryApiService {

    /**
     * Fetch dictionary entries for [word].
     * @param word the English word to look up (e.g., "ephemeral")
     * @return list of entries (typically one element)
     */
    @GET("api/v2/entries/en/{word}")
    suspend fun getDefinition(
        @Path("word") word: String
    ): List<DictionaryEntry>
}
