package com.mountaincrab.bookstore.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.data.model.BookSource
import com.mountaincrab.bookstore.ui.components.CoverPlaceholder
import com.mountaincrab.bookstore.ui.components.icon
import com.mountaincrab.bookstore.ui.theme.LocalAppPalette

private enum class SheetMode { SEARCH, FORM }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBookSheet(
    existing: BookEntity?,
    onDismiss: () -> Unit,
    viewModel: AddEditBookViewModel,
) {
    val palette = LocalAppPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var mode by remember { mutableStateOf(if (existing != null) SheetMode.FORM else SheetMode.SEARCH) }
    var query by remember { mutableStateOf("") }

    // Form fields
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var author by remember { mutableStateOf(existing?.author ?: "") }
    var genres by remember { mutableStateOf(existing?.genres?.joinToString(", ") ?: "") }
    var read by remember { mutableStateOf(existing?.read ?: false) }
    var source by remember { mutableStateOf(existing?.source ?: BookSource.BOUGHT) }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    val online by viewModel.onlineSearch.collectAsStateWithLifecycle()

    LaunchedEffect(query) { viewModel.searchOnline(query) }

    fun close() {
        viewModel.resetSearch()
        onDismiss()
    }

    fun save() {
        if (title.isBlank()) return
        val genreList = genres.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (existing != null) {
            viewModel.updateBook(existing.copy(title = title.trim(), author = author.trim().ifEmpty { "Unknown" }, genres = genreList, read = read, source = source, notes = notes.trim(), readAt = if (read) (existing.readAt ?: System.currentTimeMillis()) else null))
        } else {
            viewModel.addBook(title, author, genreList, read, source, notes)
        }
        close()
    }

    ModalBottomSheet(
        onDismissRequest = ::close,
        sheetState = sheetState,
        containerColor = palette.surfaceRaised,
    ) {
        Column(modifier = Modifier.imePadding()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (mode == SheetMode.FORM) (if (existing != null) "Edit book" else "Add details") else "Add a book",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Box(
                    modifier = Modifier.size(32.dp).background(palette.surfaceHigh, RoundedCornerShape(50)).clickable { close() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = palette.fgMuted, modifier = Modifier.size(18.dp))
                }
            }

            if (mode == SheetMode.SEARCH) {
                SearchMode(
                    query = query,
                    onQuery = { query = it },
                    online = online,
                    onPick = { result ->
                        title = result.title
                        author = result.author
                        genres = result.genres.joinToString(", ")
                        read = false
                        source = BookSource.BOUGHT
                        notes = ""
                        viewModel.resetSearch()
                        mode = SheetMode.FORM
                    },
                    onManual = { mode = SheetMode.FORM },
                )
            } else {
                FormMode(
                    title = title, onTitle = { title = it },
                    author = author, onAuthor = { author = it },
                    genres = genres, onGenres = { genres = it },
                    read = read, onRead = { read = it },
                    source = source, onSource = { source = it },
                    notes = notes, onNotes = { notes = it },
                    isEdit = existing != null,
                    canGoBack = existing == null,
                    onBack = { mode = SheetMode.SEARCH },
                    onSave = ::save,
                )
            }
        }
    }
}

@Composable
private fun SearchMode(
    query: String,
    onQuery: (String) -> Unit,
    online: OnlineSearchState,
    onPick: (com.mountaincrab.bookstore.data.remote.BookSearchResult) -> Unit,
    onManual: () -> Unit,
) {
    val palette = LocalAppPalette.current
    Column {
        // Search input
        Row(
            modifier = Modifier
                .padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 12.dp)
                .fillMaxWidth()
                .background(palette.surfaceHigh, RoundedCornerShape(12.dp))
                .border(1.dp, palette.cardBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = palette.fgMuted, modifier = Modifier.size(18.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) Text("Search by title or author…", color = palette.fgMuted, fontSize = 15.sp)
                BasicTextField(
                    value = query,
                    onValueChange = onQuery,
                    singleLine = true,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Text(
            "ONLINE RESULTS",
            color = palette.fgMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 22.dp, top = 6.dp, bottom = 8.dp),
        )

        Box(modifier = Modifier.heightIn(max = 320.dp)) {
            when {
                online.loading -> Text("Searching…", color = palette.fgFaint, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp))
                online.error != null -> Text(online.error, color = palette.fgFaint, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp))
                online.results.isEmpty() && query.isNotBlank() -> Text("No matches. Add it manually below.", color = palette.fgFaint, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp))
                else -> LazyColumn(modifier = Modifier.padding(horizontal = 12.dp)) {
                    items(online.results) { r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(r) }
                                .padding(horizontal = 10.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CoverPlaceholder()
                            Column(modifier = Modifier.weight(1f)) {
                                Text(r.title, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    r.author + (r.year?.let { " · $it" } ?: ""),
                                    color = palette.fgMuted,
                                    fontSize = 12.5.sp,
                                )
                            }
                            Icon(Icons.Filled.Add, contentDescription = "Add", tint = palette.accentText, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Manual entry button
        Box(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 24.dp)) {
            Surface(
                color = palette.surfaceHigh,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder),
                modifier = Modifier.fillMaxWidth().clickable { onManual() },
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 13.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                    Text("Enter manually", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun FormMode(
    title: String, onTitle: (String) -> Unit,
    author: String, onAuthor: (String) -> Unit,
    genres: String, onGenres: (String) -> Unit,
    read: Boolean, onRead: (Boolean) -> Unit,
    source: BookSource, onSource: (BookSource) -> Unit,
    notes: String, onNotes: (String) -> Unit,
    isEdit: Boolean,
    canGoBack: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    val palette = LocalAppPalette.current
    Column(
        modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LabeledField("TITLE") { FormInput(title, onTitle, "Book title") }
        LabeledField("AUTHOR") { FormInput(author, onAuthor, "Author name") }
        LabeledField("GENRES") { FormInput(genres, onGenres, "Fiction, Sci-fi…") }
        LabeledField("STATUS") {
            PillGroup(
                options = listOf(
                    PillOption("unread", "Unread", Icons.Filled.Bookmark),
                    PillOption("read", "Read", Icons.Filled.Check),
                ),
                selectedId = if (read) "read" else "unread",
                onSelect = { onRead(it == "read") },
            )
        }
        LabeledField("WHERE FROM") {
            PillGroup(
                options = BookSource.entries.map { PillOption(it.name, it.label, it.icon()) },
                selectedId = source.name,
                onSelect = { onSource(BookSource.valueOf(it)) },
            )
        }
        LabeledField("NOTES") { FormInput(notes, onNotes, "What did you think?", minLines = 3) }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (canGoBack) {
                Surface(
                    color = palette.surfaceHigh,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder),
                    modifier = Modifier.clickable { onBack() },
                ) {
                    Text("Back", color = palette.fgMuted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp))
                }
            }
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).clickable { onSave() },
            ) {
                Text(
                    if (isEdit) "Save changes" else "Add to shelf",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 13.dp).fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, content: @Composable () -> Unit) {
    val palette = LocalAppPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, color = palette.fgMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        content()
    }
}

@Composable
private fun FormInput(value: String, onValue: (String) -> Unit, placeholder: String, minLines: Int = 1) {
    val palette = LocalAppPalette.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.surfaceHigh, RoundedCornerShape(8.dp))
            .border(1.dp, palette.cardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        if (value.isEmpty()) Text(placeholder, color = palette.fgMuted, fontSize = 14.sp)
        BasicTextField(
            value = value,
            onValueChange = onValue,
            minLines = minLines,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private data class PillOption(val id: String, val label: String, val icon: ImageVector)

@Composable
private fun PillGroup(options: List<PillOption>, selectedId: String, onSelect: (String) -> Unit) {
    val palette = LocalAppPalette.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        options.forEach { opt ->
            val on = opt.id == selectedId
            Surface(
                color = if (on) MaterialTheme.colorScheme.primary else palette.surfaceHigh,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (on) MaterialTheme.colorScheme.primary else palette.cardBorder),
                modifier = Modifier.weight(1f).clickable { onSelect(opt.id) },
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 11.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(opt.icon, contentDescription = null, tint = if (on) Color.White else palette.fgMuted, modifier = Modifier.size(15.dp))
                    Text(opt.label, color = if (on) Color.White else palette.fgMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}
