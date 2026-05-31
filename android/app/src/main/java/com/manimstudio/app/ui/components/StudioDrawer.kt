package com.manimstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimstudio.app.data.models.RecentChat
import com.manimstudio.app.ui.theme.*

@Composable
fun StudioDrawer(
    onNewChat: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onGalleryClick: () -> Unit,
    onTemplatesClick: () -> Unit,
    recentChats: List<RecentChat>,
    onSelectChat: (RecentChat) -> Unit,
    onClose: () -> Unit,
) {
    ModalDrawerSheet(
        drawerContainerColor = Color(0xFF0A0A0A),
        modifier = Modifier.width(300.dp),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Manim Studio",
                style = MaterialTheme.typography.titleLarge,
                color = OnBackground,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.EditNote,
                    contentDescription = "New",
                    tint = OnSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // New chat — prominent
        NavigationDrawerItem(
            icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
            label = { Text("New animation", style = MaterialTheme.typography.bodyLarge) },
            selected = false,
            onClick = onNewChat,
            shape = RoundedCornerShape(percent = 50),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = SurfaceBright,
                unselectedIconColor = OnBackground,
                unselectedTextColor = OnBackground,
            ),
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        Spacer(modifier = Modifier.height(4.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            label = { Text("Search", style = MaterialTheme.typography.bodyLarge) },
            selected = false,
            onClick = { /* search */ },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent,
                unselectedIconColor = OnSurfaceVariant,
                unselectedTextColor = OnBackground,
            ),
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Outlined.VideoLibrary, contentDescription = null) },
            label = { Text("Gallery", style = MaterialTheme.typography.bodyLarge) },
            selected = false,
            onClick = onGalleryClick,
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent,
                unselectedIconColor = OnSurfaceVariant,
                unselectedTextColor = OnBackground,
            ),
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Outlined.GridView, contentDescription = null) },
            label = { Text("Templates", style = MaterialTheme.typography.bodyLarge) },
            selected = false,
            onClick = onTemplatesClick,
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent,
                unselectedIconColor = OnSurfaceVariant,
                unselectedTextColor = OnBackground,
            ),
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        // Recent section
        if (recentChats.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recent",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceDim,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                letterSpacing = 0.5.sp,
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
            ) {
                items(recentChats, key = { it.id }) { chat ->
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = chat.title,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        selected = false,
                        onClick = { onSelectChat(chat) },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            unselectedTextColor = OnSurface,
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // Bottom — user profile + settings
        HorizontalDivider(color = OutlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "A",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Abdulfatai",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnBackground,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "PRO",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    letterSpacing = 1.sp,
                )
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}
