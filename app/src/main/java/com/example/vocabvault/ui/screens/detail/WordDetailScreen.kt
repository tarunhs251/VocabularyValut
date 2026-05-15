package com.example.vocabvault.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vocabvault.data.local.WordEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    wordId: Int,
    onNavigateBack: () -> Unit,
    viewModel: WordDetailViewModel = hiltViewModel()
) {
    val entity by viewModel.word.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(wordId) {
        viewModel.loadWord(wordId)
    }

    LaunchedEffect(viewModel.deleted) {
        if (viewModel.deleted) onNavigateBack()
    }

    if (entity == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val word = entity!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(word.word) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Star toggle
                    IconButton(onClick = { viewModel.toggleStar(word) }) {
                        Icon(
                            imageVector = if (word.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (word.isStarred) "Unstar" else "Star",
                            tint = if (word.isStarred) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Delete
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (word.partOfSpeech.isNotBlank()) {
                SuggestionChip(onClick = {}, label = { Text(word.partOfSpeech) })
            }

            DetailSection(title = "Meaning", body = word.meaning)

            if (word.example.isNotBlank()) {
                DetailSection(title = "Example", body = "\"${word.example}\"")
            }

            if (word.synonyms.isNotBlank()) {
                DetailSection(title = "Synonyms", body = word.synonyms)
            }

            HorizontalDivider()

            // Quiz stats
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                QuizStat(label = "Correct", value = word.correctCount.toString())
                QuizStat(label = "Wrong", value = word.wrongCount.toString())
                val total = word.correctCount + word.wrongCount
                val accuracy = if (total == 0) "–" else "${word.correctCount * 100 / total}%"
                QuizStat(label = "Accuracy", value = accuracy)
            }

            HorizontalDivider()

            Text(
                text = "Added: ${formatDate(word.dateAdded)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete \"${word.word}\"?") },
            text = { Text("This will permanently remove the word and all its quiz history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteWord(word)
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DetailSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(body, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun QuizStat(label: String, value: String) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatDate(epochMs: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(epochMs))
}
