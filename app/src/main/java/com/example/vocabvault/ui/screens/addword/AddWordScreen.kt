package com.example.vocabvault.ui.screens.addword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddWordViewModel = hiltViewModel()
) {
    val wordInput by viewModel.wordInput.collectAsStateWithLifecycle()
    val meaningInput by viewModel.meaningInput.collectAsStateWithLifecycle()
    val exampleInput by viewModel.exampleInput.collectAsStateWithLifecycle()
    val synonymsInput by viewModel.synonymsInput.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate back automatically after a successful save
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddWordUiState.Saved -> {
                viewModel.reset()
                onNavigateBack()
            }
            is AddWordUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            is AddWordUiState.Duplicate -> {
                snackbarHostState.showSnackbar("\"${state.word}\" is already in your vocabulary.")
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Word") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Word input + Fetch button ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = wordInput,
                    onValueChange = viewModel::onWordInputChange,
                    label = { Text("Word") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is AddWordUiState.Loading
                )

                Button(
                    onClick = viewModel::fetchDefinition,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically),
                    enabled = wordInput.isNotBlank() && uiState !is AddWordUiState.Loading
                ) {
                    if (uiState is AddWordUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Fetch")
                    }
                }
            }

            // Offline hint shown after an API error
            if (uiState is AddWordUiState.Error) {
                Text(
                    text = "Could not fetch definition. Fill in manually below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // ── Editable meaning ──────────────────────────────────────────────
            OutlinedTextField(
                value = meaningInput,
                onValueChange = viewModel::onMeaningInputChange,
                label = { Text("Meaning *") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Editable example ──────────────────────────────────────────────
            OutlinedTextField(
                value = exampleInput,
                onValueChange = viewModel::onExampleInputChange,
                label = { Text("Example sentence (optional)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Editable synonyms ─────────────────────────────────────────────
            OutlinedTextField(
                value = synonymsInput,
                onValueChange = viewModel::onSynonymsInputChange,
                label = { Text("Synonyms (comma separated)") },
                minLines = 1,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // ── Save button ───────────────────────────────────────────────────
            Button(
                onClick = viewModel::saveWord,
                modifier = Modifier.fillMaxWidth(),
                enabled = wordInput.isNotBlank() && meaningInput.isNotBlank()
                        && uiState !is AddWordUiState.Loading
            ) {
                Text("Save to Vocabulary")
            }
        }
    }
}
