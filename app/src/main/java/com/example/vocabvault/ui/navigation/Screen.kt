package com.example.vocabvault.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

/** All destinations reachable from the bottom navigation bar. */
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Quiz : Screen("quiz", "Quiz", Icons.Default.Psychology)
    object Stats : Screen("stats", "Stats", Icons.Default.BarChart)
}

/** Destinations NOT in the bottom bar (navigated to programmatically). */
object Routes {
    const val ADD_WORD = "add_word"
    const val WORD_DETAIL = "word_detail/{wordId}"

    fun wordDetail(id: Int) = "word_detail/$id"
}

/** Bottom navigation items in display order. */
val bottomNavItems = listOf(Screen.Home, Screen.Search, Screen.Quiz, Screen.Stats)
