package com.mountaincrab.bookstore.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.ui.theme.LocalAppPalette

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookDetailSheet(
    book: BookEntity,
    onDismiss: () -> Unit,
    onToggleRead: () -> Unit,
    onEdit: () -> Unit,
) {
    val palette = LocalAppPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surfaceRaised,
    ) {
        Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 24.dp)) {
            // Read pill + close
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReadPill(read = book.read)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(palette.surfaceHigh, CircleShape)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = palette.fgMuted, modifier = Modifier.size(18.dp))
                }
            }

            Text(book.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
            Row(modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)) {
                Text("by ", color = palette.fgMuted, fontSize = 15.sp)
                Text(book.author, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 18.dp)) {
                book.genres.forEach { GenreChip(it) }
                SourceChip(book)
            }

            // Notes block
            Surface(color = palette.surfaceHigh, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text("NOTES", color = palette.fgMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(
                        book.notes.ifBlank { "No notes yet." },
                        color = if (book.notes.isBlank()) palette.fgFaint else androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = if (book.read) palette.surfaceHigh else androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp),
                    border = if (book.read) androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder) else null,
                    modifier = Modifier.weight(1f).clickable { onToggleRead() },
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            if (book.read) Icons.Outlined.Circle else Icons.Filled.Check,
                            contentDescription = null,
                            tint = if (book.read) androidx.compose.material3.MaterialTheme.colorScheme.onSurface else Color.White,
                            modifier = Modifier.size(17.dp),
                        )
                        Text(
                            if (book.read) "Mark as unread" else "Mark as read",
                            color = if (book.read) androidx.compose.material3.MaterialTheme.colorScheme.onSurface else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
                Surface(
                    color = palette.surfaceHigh,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier.clickable { onEdit() },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, tint = palette.fgMuted, modifier = Modifier.size(16.dp))
                        Text("Edit", color = palette.fgMuted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadPill(read: Boolean) {
    val palette = LocalAppPalette.current
    val bg = if (read) palette.successSoft else palette.surfaceHigh
    val fg = if (read) palette.successText else palette.fgMuted
    Surface(color = bg, shape = CircleShape) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(if (read) Icons.Filled.Check else Icons.Outlined.Circle, contentDescription = null, tint = fg, modifier = Modifier.size(13.dp))
            Text(if (read) "Read" else "Unread", color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SourceChip(book: BookEntity) {
    val palette = LocalAppPalette.current
    Surface(color = palette.surfaceHigh, shape = CircleShape) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(book.source.icon(), contentDescription = null, tint = palette.fgMuted, modifier = Modifier.size(11.dp))
            Text(book.source.label, color = palette.fgMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
