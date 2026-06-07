package com.mountaincrab.bookstore.ui.read

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.ui.components.BookRow
import com.mountaincrab.bookstore.ui.components.SectionHeader
import com.mountaincrab.bookstore.ui.theme.LocalAppPalette
import org.koin.compose.viewmodel.koinViewModel

private enum class GroupBy(val label: String, val icon: ImageVector) {
    RECENT("Recent", Icons.Filled.Schedule),
    AUTHOR("Author", Icons.Filled.Person),
    GENRE("Genre", Icons.Filled.Sell),
}

private data class Section(val label: String, val meta: String, val items: List<BookEntity>)

private fun lastName(author: String): String = author.trim().split(' ').lastOrNull().orEmpty()

private fun sectionsFor(group: GroupBy, books: List<BookEntity>): List<Section> = when (group) {
    GroupBy.RECENT -> listOf(Section("Recently read", books.size.toString(), books))
    GroupBy.AUTHOR -> books.groupBy { it.author }
        .toSortedMap(compareBy { lastName(it).lowercase() })
        .map { (author, items) -> Section(author, "${items.size} ${if (items.size == 1) "book" else "books"}", items) }
    GroupBy.GENRE -> books.groupBy { it.genres.firstOrNull() ?: "Unsorted" }
        .toSortedMap(compareBy { it.lowercase() })
        .map { (genre, items) -> Section(genre, items.size.toString(), items) }
}

@Composable
fun ReadScreen(
    onOpenBook: (BookEntity) -> Unit,
    onAddBook: () -> Unit,
    viewModel: ReadViewModel = koinViewModel(),
) {
    val palette = LocalAppPalette.current
    val books by viewModel.readBooks.collectAsStateWithLifecycle()
    var group by remember { mutableStateOf(GroupBy.RECENT) }

    val authorCount = remember(books) { books.map { it.author }.distinct().size }
    val sections = remember(group, books) { sectionsFor(group, books) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp)) {
                Text("Read", color = MaterialTheme.colorScheme.onSurface, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    "${books.size} books · $authorCount authors",
                    color = palette.fgMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            // Group-by control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Group by", color = palette.fgFaint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    GroupBy.entries.forEach { opt ->
                        val on = opt == group
                        Row(
                            modifier = Modifier
                                .background(if (on) palette.surfaceHigh else Color.Transparent, CircleShape)
                                .clickable { group = opt }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(opt.icon, contentDescription = null, tint = if (on) MaterialTheme.colorScheme.onSurface else palette.fgMuted, modifier = Modifier.size(13.dp))
                            Text(opt.label, color = if (on) MaterialTheme.colorScheme.onSurface else palette.fgMuted, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            if (books.isEmpty()) {
                EmptyRead(onAddBook)
            } else {
                LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 96.dp)) {
                    sections.forEach { section ->
                        item(key = "h-${section.label}") {
                            SectionHeader(section.label, section.meta)
                        }
                        items(section.items.size, key = { section.items[it].id }) { idx ->
                            BookRow(section.items[idx], onClick = { onOpenBook(section.items[idx]) })
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddBook,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 24.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add book", modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
private fun EmptyRead(onAddBook: () -> Unit) {
    val palette = LocalAppPalette.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("No books read yet", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "Search for a book or add one with the + button to start your shelf.",
            color = palette.fgMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
