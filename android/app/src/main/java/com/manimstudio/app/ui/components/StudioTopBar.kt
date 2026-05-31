package com.manimstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.manimstudio.app.ui.theme.OnBackground
import com.manimstudio.app.ui.theme.OnSurfaceVariant

@Composable
fun StudioTopBar(
    currentEngine: String,
    onMenuClick: () -> Unit,
    onEngineClick: () -> Unit,
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
        // Hamburger
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = "Menu",
                tint = OnBackground,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Engine selector — center
        TextButton(
            onClick = onEngineClick,
            shape = RoundedCornerShape(percent = 50),
        ) {
            Text(
                text = currentEngine,
                style = MaterialTheme.typography.titleMedium,
                color = OnBackground,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // New chat / edit icon
        IconButton(onClick = onNewChatClick) {
            Icon(
                imageVector = Icons.Outlined.EditNote,
                contentDescription = "New animation",
                tint = OnBackground,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
