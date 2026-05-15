package com.example.vocabvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vocabvault.ui.navigation.NavGraph
import com.example.vocabvault.ui.navigation.Routes
import com.example.vocabvault.ui.navigation.Screen
import com.example.vocabvault.ui.navigation.bottomNavItems
import com.example.vocabvault.ui.theme.VocabVaultTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VocabVaultTheme {
                val navController = rememberNavController()
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route

                // Bottom nav is hidden on Add Word and Detail screens
                val showBottomBar = currentRoute !in listOf(Routes.ADD_WORD, Routes.WORD_DETAIL)

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                modifier = Modifier.height(80.dp)
                            ) {
                                bottomNavItems.forEach { screen ->
                                    NavigationBarItem(
                                        selected = currentRoute == screen.route,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                // Pop up to start to avoid deep back stack
                                                popUpTo(Screen.Home.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                                        label = { Text(screen.label) }
                                    )
                                }
                            }
                        }
                    }
                ) { _ ->
                    NavGraph(
                        navController = navController
                    )
                    // innerPadding applied inside each screen via Scaffold paddingValues
                }
            }
        }
    }
}
