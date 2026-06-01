package com.manimstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StudioTopBar(
    onMenuClick: () -> Unit,
    onNewChatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onMenuClick) {
            // Use three horizontal lines (hamburger) — proper M3 icon
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = "Open menu",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        // No engine selector here anymore
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onNewChatClick) {
            Icon(
                imageVector = Icons.Outlined.EditNote,
                contentDescription = "New animation",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
