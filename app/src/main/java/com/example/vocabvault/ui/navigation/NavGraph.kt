package com.example.vocabvault.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.vocabvault.ui.screens.addword.AddWordScreen
import com.example.vocabvault.ui.screens.detail.WordDetailScreen
import com.example.vocabvault.ui.screens.home.HomeScreen
import com.example.vocabvault.ui.screens.quiz.QuizScreen
import com.example.vocabvault.ui.screens.search.SearchScreen
import com.example.vocabvault.ui.screens.stats.StatsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onAddWord = { navController.navigate(Routes.ADD_WORD) },
                onWordClick = { wordId -> navController.navigate(Routes.wordDetail(wordId)) }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onWordClick = { wordId -> navController.navigate(Routes.wordDetail(wordId)) }
            )
        }

        composable(Screen.Quiz.route) {
            QuizScreen()
        }

        composable(Screen.Stats.route) {
            StatsScreen()
        }

        composable(Routes.ADD_WORD) {
            AddWordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.WORD_DETAIL,
            arguments = listOf(navArgument("wordId") { type = NavType.IntType })
        ) { backStackEntry ->
            val wordId = backStackEntry.arguments?.getInt("wordId") ?: return@composable
            WordDetailScreen(
                wordId = wordId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
