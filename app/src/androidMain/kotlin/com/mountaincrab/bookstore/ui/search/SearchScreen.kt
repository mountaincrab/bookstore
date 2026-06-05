package com.mountaincrab.bookstore.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.ui.components.ReadBadge
import com.mountaincrab.bookstore.ui.components.ReadToggle
import com.mountaincrab.bookstore.ui.components.SourceTag
import com.mountaincrab.bookstore.ui.theme.LocalAppPalette
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchScreen(
    onOpenBook: (BookEntity) -> Unit,
    onAddBook: () -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val palette = LocalAppPalette.current
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val readCount = results.count { it.read }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar + add button
        Surface(color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(palette.surfaceHigh, CircleShape)
                        .border(1.dp, palette.cardBorder, CircleShape)
                        .padding(horizontal = 16.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = palette.fgMuted, modifier = Modifier.size(18.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text("Search every book", color = palette.fgMuted, fontSize = 15.sp)
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = viewModel::onQueryChange,
                            singleLine = true,
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (query.isNotEmpty()) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Clear",
                            tint = palette.fgFaint,
                            modifier = Modifier.size(17.dp).clickable { viewModel.onQueryChange("") },
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        .clickable { onAddBook() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add a book", tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
        }

        // Count line
        Row(
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 13.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("${results.size} ${if (results.size == 1) "book" else "books"}", color = palette.fgFaint, fontSize = 12.sp)
            if (readCount > 0) {
                Text("$readCount read", color = palette.successText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (results.isEmpty()) {
            EmptySearch(query, onAddBook)
        } else {
            LazyColumn(contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 2.dp, bottom = 24.dp)) {
                items(results, key = { it.id }) { book ->
                    SearchRow(
                        book = book,
                        onOpen = { onOpenBook(book) },
                        onToggle = { viewModel.setRead(book.id, !book.read) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchRow(book: BookEntity, onOpen: () -> Unit, onToggle: () -> Unit) {
    val palette = LocalAppPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .padding(horizontal = 8.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    book.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (book.read) ReadBadge()
            }
            Text(book.author, color = palette.fgMuted, fontSize = 12.5.sp, modifier = Modifier.padding(top = 2.dp))
            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                book.genres.firstOrNull()?.let { Text(it, color = palette.fgFaint, fontSize = 11.5.sp) }
                SourceTag(book.source)
            }
        }
        ReadToggle(read = book.read, onToggle = onToggle)
    }
}

@Composable
private fun EmptySearch(query: String, onAddBook: () -> Unit) {
    val palette = LocalAppPalette.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = palette.fgFaint, modifier = Modifier.size(26.dp))
        Text(
            if (query.isBlank()) "Your shelf is empty." else "No books match \"$query\".",
            color = palette.fgFaint,
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 12.dp),
        )
        Surface(
            color = palette.surfaceHigh,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.clickable { onAddBook() },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(15.dp))
                Text("Add a book", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
