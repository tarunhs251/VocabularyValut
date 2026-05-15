package com.example.vocabvault.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabvault.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class DailyWordCount(
    val dayName: String,
    val count: Int
)

data class StatsData(
    val totalWords: Int = 0,
    val starredCount: Int = 0,
    val thisWeekCount: Int = 0,
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0
) {
    /** Quiz accuracy as a percentage (0–100). */
    val accuracyPercent: Int
        get() {
            val total = totalCorrect + totalWrong
            return if (total == 0) 0 else (totalCorrect * 100 / total)
        }
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: WordRepository
) : ViewModel() {

    /**
     * Combines five independent DB flows into a single [StatsData] snapshot.
     * Any change in the DB automatically triggers a recomposition.
     */
    val stats: StateFlow<StatsData> = combine(
        repository.getTotalCount(),
        repository.getStarredCount(),
        repository.getCountThisWeek(),
        repository.getTotalCorrect(),
        repository.getTotalWrong()
    ) { total, starred, thisWeek, correct, wrong ->
        StatsData(
            totalWords = total,
            starredCount = starred,
            thisWeekCount = thisWeek,
            totalCorrect = correct ?: 0,
            totalWrong = wrong ?: 0
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsData()
    )

    /** Provides daily word count for the past 7 days. For charting. */
    val weeklyData: StateFlow<List<DailyWordCount>> = repository.getAllWords()
        .map { words ->
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L
            
            // Count words added for each of the last 7 days
            val days = (0..6).map { daysAgo ->
                val dayStart = now - (daysAgo * dayMs)
                val dayEnd = dayStart - dayMs
                val count = words.count { 
                    it.dateAdded in (dayEnd + 1)..dayStart
                }
                
                val dayNames = arrayOf("Today", "Yest.", "2d", "3d", "4d", "5d", "6d")
                DailyWordCount(dayName = dayNames[daysAgo], count = count)
            }
            days.reversed() // Oldest first
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
