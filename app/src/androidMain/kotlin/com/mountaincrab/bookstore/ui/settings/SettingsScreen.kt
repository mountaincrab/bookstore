package com.mountaincrab.bookstore.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountaincrab.bookstore.BuildConfig
import com.mountaincrab.bookstore.ui.theme.AppTheme
import com.mountaincrab.bookstore.ui.theme.LocalAppPalette
import com.mountaincrab.bookstore.ui.theme.ThemeViewModel
import org.koin.compose.viewmodel.koinViewModel

// Representative swatch colours per theme (background, accent).
private fun swatch(theme: AppTheme): Pair<Color, Color> = when (theme) {
    AppTheme.DEEP_NAVY -> Color(0xFF0A1020) to Color(0xFF4F7CFF)
    AppTheme.CHARCOAL -> Color(0xFF0A0A0A) to Color(0xFF06B6D4)
    AppTheme.RETRO -> Color(0xFF1A0B1E) to Color(0xFFFF00CC)
}

@Composable
fun SettingsScreen(themeViewModel: ThemeViewModel = koinViewModel()) {
    val palette = LocalAppPalette.current
    val current by themeViewModel.appTheme.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text("Settings", color = MaterialTheme.colorScheme.onSurface, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)

        Text(
            "THEME",
            color = palette.fgMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
        )
        Surface(color = palette.surfaceRaised, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, palette.cardBorder), modifier = Modifier.fillMaxWidth()) {
            Column {
                AppTheme.entries.forEach { theme ->
                    val (bg, accent) = swatch(theme)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { themeViewModel.setTheme(theme) }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(modifier = Modifier.size(28.dp).background(bg, CircleShape).padding(6.dp), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(14.dp).background(accent, CircleShape))
                        }
                        Text(theme.displayName, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        if (theme == current) {
                            Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        Text(
            "ABOUT",
            color = palette.fgMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 28.dp, bottom = 10.dp),
        )
        Surface(color = palette.surfaceRaised, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, palette.cardBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Book Shelf", color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "A personal reading tracker. Data is stored locally on this device. " +
                        "Cloud sync (Firebase) is built but not enabled yet.",
                    color = palette.fgMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(
                    "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    color = palette.fgMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }
}
