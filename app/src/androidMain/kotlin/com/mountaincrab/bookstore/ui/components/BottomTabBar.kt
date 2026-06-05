package com.mountaincrab.bookstore.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountaincrab.bookstore.ui.theme.LocalAppPalette

enum class BookTab(val label: String, val icon: ImageVector) {
    READ("Read", Icons.AutoMirrored.Filled.MenuBook),
    SEARCH("Search", Icons.Filled.Search),
    SETTINGS("Settings", Icons.Filled.Settings),
}

/** Bottom nav with a pill-shaped active indicator, matching the design's TabBar. */
@Composable
fun BottomTabBar(active: BookTab, onSelect: (BookTab) -> Unit) {
    val palette = LocalAppPalette.current
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            BookTab.entries.forEach { tab ->
                val on = tab == active
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(30.dp)
                            .background(if (on) palette.accentSoft else Color.Transparent, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            tab.icon,
                            contentDescription = tab.label,
                            tint = if (on) palette.accentText else palette.fgMuted,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Text(
                        tab.label,
                        color = if (on) MaterialTheme.colorScheme.onSurface else palette.fgMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
