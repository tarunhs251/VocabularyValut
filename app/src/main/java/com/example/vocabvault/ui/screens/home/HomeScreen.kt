package com.example.vocabvault.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vocabvault.ui.components.WordCard

private val TAB_TITLES = listOf("All", "Starred", "This Week")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddWord: () -> Unit,
    onWordClick: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val words by viewModel.words.collectAsStateWithLifecycle()
    val isBulkMode by viewModel.isBulkDeleteMode.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedWordIds.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (isBulkMode && selectedIds.isNotEmpty()) {
                // Bulk delete mode top bar
                TopAppBar(
                    title = { Text("${selectedIds.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.toggleBulkDeleteMode() }) {
                            Icon(Icons.Default.Close, contentDescription = "Exit bulk mode")
                        }
                    },
                    actions = {
                        if (selectedIds.size < words.size) {
                            IconButton(onClick = { viewModel.selectAllShown(words) }) {
                                Text("Select All", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete selected")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        titleContentColor = MaterialTheme.colorScheme.onErrorContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onErrorContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            } else {
                // Normal mode top bar
                TopAppBar(
                    title = { Text("VocabVault") },
                    actions = {
                        if (words.isNotEmpty()) {
                            IconButton(onClick = { viewModel.toggleBulkDeleteMode() }) {
                                Icon(Icons.Default.Delete, contentDescription = "Bulk delete mode")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        floatingActionButton = {
            if (!isBulkMode) {
                ExtendedFloatingActionButton(
                    onClick = onAddWord,
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add word") },
                    text = { Text("Add Word") },
                    modifier = Modifier.padding(bottom = 80.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Tab row for All / Starred / This Week
            TabRow(
                selectedTabIndex = selectedTab,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab])
                    )
                }
            ) {
                TAB_TITLES.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text(title) }
                    )
                }
            }

            if (words.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (selectedTab) {
                                HomeTabs.STARRED -> "No starred words yet.\nTap ★ on any word to star it."
                                HomeTabs.THIS_WEEK -> "No words added this week.\nTap + to add one!"
                                else -> "Your vocabulary list is empty.\nTap + to add your first word!"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 8.dp,
                        bottom = 88.dp  // avoid FAB overlap
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(words, key = { it.id }) { entity ->
                        if (isBulkMode) {
                            BulkSelectWordCard(
                                entity = entity,
                                isSelected = selectedIds.contains(entity.id),
                                onToggleSelect = { viewModel.toggleWordSelection(entity.id) },
                                onCardClick = { onWordClick(entity.id) },
                                onStarClick = { viewModel.toggleStar(entity) }
                            )
                        } else {
                            WordCard(
                                entity = entity,
                                onCardClick = { onWordClick(entity.id) },
                                onStarClick = { viewModel.toggleStar(entity) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete ${selectedIds.size} word(s)?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteSelected()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/** Word card variant for bulk selection mode with a checkbox. */
@Composable
private fun BulkSelectWordCard(
    entity: com.example.vocabvault.data.local.WordEntity,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onCardClick: () -> Unit,
    onStarClick: () -> Unit
) {
    Card(
        onClick = { onToggleSelect() },
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.run {
                    background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                }
                else Modifier
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() }
            )

            // Word info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entity.word,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = entity.meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            // Star icon
            IconButton(onClick = onStarClick) {
                Icon(
                    imageVector = if (entity.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (entity.isStarred) "Unstar" else "Star",
                    tint = if (entity.isStarred) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
