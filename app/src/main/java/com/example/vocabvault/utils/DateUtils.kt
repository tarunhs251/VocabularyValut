package com.example.vocabvault.utils

/** Date/time helpers used across the app. */
object DateUtils {
    private const val MILLIS_PER_DAY = 86_400_000L

    /** Returns the epoch-ms timestamp for exactly 7 days ago. */
    fun weekAgoMillis(): Long = System.currentTimeMillis() - 7 * MILLIS_PER_DAY
}
