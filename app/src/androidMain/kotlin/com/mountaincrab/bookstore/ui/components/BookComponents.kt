package com.mountaincrab.bookstore.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.data.model.BookSource
import com.mountaincrab.bookstore.ui.theme.LocalAppPalette

/** Two-letter author initials, e.g. "Ursula K. Le Guin" -> "UL". */
fun authorInitials(author: String): String {
    val parts = author.split(' ').filter { it.isNotBlank() }
    if (parts.isEmpty()) return "?"
    val first = parts.first().firstOrNull()?.uppercaseChar() ?: ""
    val last = if (parts.size > 1) parts.last().firstOrNull()?.uppercaseChar() ?: "" else ""
    return "$first$last".ifEmpty { "?" }
}

fun BookSource.icon(): ImageVector = when (this) {
    BookSource.LIBRARY -> Icons.Filled.LocalLibrary
    BookSource.BOUGHT -> Icons.Filled.ShoppingBag
    BookSource.BORROWED -> Icons.Filled.People
}

/** Rounded square tile with the author's initials (BRow leading element). */
@Composable
fun MonogramTile(author: String, modifier: Modifier = Modifier) {
    val palette = LocalAppPalette.current
    Box(
        modifier = modifier
            .size(42.dp)
            .background(palette.surfaceHigh, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = authorInitials(author),
            color = palette.accentText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun GenreChip(text: String) {
    val palette = LocalAppPalette.current
    Surface(
        color = palette.surfaceHigh,
        shape = CircleShape,
    ) {
        Text(
            text = text,
            color = palette.fgMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun SourceTag(source: BookSource) {
    val palette = LocalAppPalette.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Icon(source.icon(), contentDescription = null, tint = palette.fgFaint, modifier = Modifier.size(13.dp))
        Text(source.label, color = palette.fgFaint, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

/** Tap-to-toggle: empty ring = unread, filled green check = read. */
@Composable
fun ReadToggle(read: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val green = MaterialTheme.colorScheme.tertiary
    Box(
        modifier = modifier
            .size(30.dp)
            .background(if (read) green else androidx.compose.ui.graphics.Color.Transparent, CircleShape)
            .border(2.dp, if (read) green else MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), CircleShape)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center,
    ) {
        if (read) {
            Icon(Icons.Filled.Check, contentDescription = "Read", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(17.dp))
        }
    }
}

/** Small "Read" pill shown next to a title in search results. */
@Composable
fun ReadBadge() {
    val palette = LocalAppPalette.current
    Surface(color = palette.successSoft, shape = CircleShape) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(start = 7.dp, end = 9.dp, top = 3.dp, bottom = 3.dp),
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = palette.successText, modifier = Modifier.size(12.dp))
            Text("Read", color = palette.successText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** A book row on the Read screen (the design's BRow): monogram, title, author, chips. */
@Composable
fun BookRow(book: BookEntity, onClick: () -> Unit) {
    val palette = LocalAppPalette.current
    Surface(
        color = palette.surfaceRaised,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, palette.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MonogramTile(book.author)
            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                Text(
                    book.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(book.author, color = palette.fgMuted, fontSize = 12.5.sp, modifier = Modifier.padding(top = 1.dp))
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    book.genres.take(2).forEach { GenreChip(it) }
                    SourceTag(book.source)
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = palette.fgFaint,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** Section header (label + meta count) used by the Read screen's grouping. */
@Composable
fun SectionHeader(label: String, meta: String) {
    val palette = LocalAppPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(meta, color = palette.fgFaint, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Placeholder cover tile for online search results. */
@Composable
fun CoverPlaceholder(modifier: Modifier = Modifier) {
    val palette = LocalAppPalette.current
    Box(
        modifier = modifier
            .width(38.dp)
            .height(50.dp)
            .background(palette.surfaceHigh, RoundedCornerShape(5.dp))
            .border(1.dp, palette.cardBorder, RoundedCornerShape(5.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = palette.fgFaint, modifier = Modifier.size(18.dp))
    }
}
