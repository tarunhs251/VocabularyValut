package com.example.vocabvault.ui.screens.quiz

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(viewModel: QuizViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Quiz - MCQ Mode") }) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is QuizUiState.Idle -> IdleContent(onStart = { viewModel.startQuiz() })
                is QuizUiState.Loading -> CircularProgressIndicator()
                is QuizUiState.Error -> ErrorContent(message = state.message, onRetry = { viewModel.startQuiz() })
                is QuizUiState.Active -> ActiveQuizContent(
                    state = state,
                    onSelectOption = viewModel::selectOption,
                    onNextQuestion = viewModel::nextQuestion
                )
                is QuizUiState.Finished -> FinishedContent(
                    score = state.score,
                    onRestart = { viewModel.startQuiz() },
                    onQuit = { viewModel.resetQuiz() }
                )
            }
        }
    }
}

@Composable
private fun IdleContent(onStart: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Text(
            "Ready for MCQ Quiz?",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            "Test your vocabulary with 20 multiple-choice questions.\nChoose the correct answer from 4 options.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(onClick = onStart, modifier = Modifier.size(120.dp, 48.dp)) {
            Text("Start Quiz")
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) { Text("Try Again") }
    }
}

@Composable
private fun ActiveQuizContent(
    state: QuizUiState.Active,
    onSelectOption: (String) -> Unit,
    onNextQuestion: () -> Unit
) {
    val card = state.card

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Progress row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question ${state.score.total + 1}/20",
                style = MaterialTheme.typography.labelLarge
            )
            LinearProgressIndicator(
                progress = { (state.score.total + 1) / 20f },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )
            Text(
                text = "Score: ${state.score.correct}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Question card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (card.mode == QuizMode.MEANING_TO_WORD) "What word means…" else "What is the meaning of…",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (card.mode == QuizMode.MEANING_TO_WORD) card.entity.meaning else card.entity.word,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // MCQ Options
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            card.options.forEach { option ->
                McqOptionButton(
                    option = option,
                    isSelected = option.id == card.selectedOptionId,
                    isRevealed = card.isAnswerRevealed,
                    onClick = { if (!card.isAnswerRevealed) onSelectOption(option.id) }
                )
            }
        }

        // Feedback and Next button
        AnimatedVisibility(
            visible = card.isAnswerRevealed,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Feedback text
                Text(
                    text = if (card.isCorrectSelected) "✓ Correct!" else "✗ Incorrect",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (card.isCorrectSelected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                // Show correct answer if wrong
                if (!card.isCorrectSelected) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F5E9)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Correct answer:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = card.options.find { it.isCorrect }?.text ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF1B5E20),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Next button
                Button(
                    onClick = onNextQuestion,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Next Question")
                }
            }
        }
    }
}

@Composable
private fun McqOptionButton(
    option: QuizOption,
    isSelected: Boolean,
    isRevealed: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        !isRevealed && isSelected -> MaterialTheme.colorScheme.secondaryContainer
        isRevealed && option.isCorrect -> Color(0xFFE8F5E9) // Light green
        isRevealed && isSelected && !option.isCorrect -> Color(0xFFFFEBEE) // Light red
        isRevealed -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isRevealed && option.isCorrect -> Color(0xFF4CAF50) // Green
        isRevealed && isSelected && !option.isCorrect -> MaterialTheme.colorScheme.error
        isSelected && !isRevealed -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val borderWidth = if (isSelected && !isRevealed || isRevealed) 2.dp else 1.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isRevealed) { onClick() }
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected && !isRevealed) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Visual indicator
            if (isRevealed) {
                Text(
                    text = if (option.isCorrect) "✓" else if (isSelected) "✗" else "",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (option.isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
            }

            Text(
                text = option.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FinishedContent(score: QuizScore, onRestart: () -> Unit, onQuit: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Text(
            "Quiz Finished!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        // Score card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${score.correct}/${score.total}",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Accuracy: ${score.accuracyPercent}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onQuit,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back Home")
            }
            Button(
                onClick = onRestart,
                modifier = Modifier.weight(1f)
            ) {
                Text("Try Again")
            }
        }
    }
}
