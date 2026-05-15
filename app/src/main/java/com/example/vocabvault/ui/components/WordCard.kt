package com.example.vocabvault.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vocabvault.data.local.WordEntity

/**
 * Reusable card displaying a word's headline info.
 * Used in Home, Search, and any list screen.
 *
 * @param onCardClick navigates to the detail screen for this word
 * @param onStarClick toggles the star state
 */
@Composable
fun WordCard(
    entity: WordEntity,
    onCardClick: () -> Unit,
    onStarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Word + part of speech chip on the same row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entity.word,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.alignByBaseline()
                    )
                    if (entity.partOfSpeech.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = entity.partOfSpeech,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier
                                .height(24.dp)
                                .alignByBaseline()
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Meaning preview — truncated to 2 lines
                Text(
                    text = entity.meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Star icon button
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
