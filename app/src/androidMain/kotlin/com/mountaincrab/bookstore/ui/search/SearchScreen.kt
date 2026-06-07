package com.mountaincrab.bookstore.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.data.remote.BookSearchResult
import com.mountaincrab.bookstore.ui.components.CoverPlaceholder
import com.mountaincrab.bookstore.ui.theme.LocalAppPalette
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchScreen(
    onAddFromResult: (BookSearchResult) -> Unit,
    onAddManual: () -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val palette = LocalAppPalette.current
    val title by viewModel.title.collectAsStateWithLifecycle()
    val author by viewModel.author.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val readBooks by viewModel.readBooks.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp)) {
            Text("Search", color = MaterialTheme.colorScheme.onSurface, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                "Find a book on Open Library to add to your shelf",
                color = palette.fgMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        // Title + Author fields
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SearchField(
                value = title,
                onValue = viewModel::onTitleChange,
                placeholder = "Title",
                imeAction = ImeAction.Next,
                onSearch = viewModel::search,
            )
            SearchField(
                value = author,
                onValue = viewModel::onAuthorChange,
                placeholder = "Author",
                imeAction = ImeAction.Search,
                onSearch = viewModel::search,
            )
            Surface(
                color = if (viewModel.canSearch) MaterialTheme.colorScheme.primary else palette.surfaceHigh,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = viewModel.canSearch) { viewModel.search() },
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 13.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = if (viewModel.canSearch) Color.White else palette.fgMuted,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        "Search",
                        color = if (viewModel.canSearch) Color.White else palette.fgMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            if (state.searched) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.clear() }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = null,
                        tint = palette.fgMuted,
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        "Clear results",
                        color = palette.fgMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 5.dp),
                    )
                }
            }
        }

        // Results / states
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.loading -> Hint("Searching…")
                state.error != null -> Hint(state.error!!)
                !state.searched -> Hint("Enter a title or author, then tap Search.")
                state.results.isEmpty() -> NoMatches(onAddManual)
                else -> LazyColumn(contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 24.dp)) {
                    items(state.results) { r ->
                        ResultRow(
                            r = r,
                            isRead = readBooks.isAlreadyRead(r),
                            onClick = { onAddFromResult(r) },
                        )
                    }
                }
            }
        }
    }
}

private fun List<BookEntity>.isAlreadyRead(result: BookSearchResult): Boolean =
    any {
        it.title.trim().equals(result.title.trim(), ignoreCase = true) &&
            it.author.trim().equals(result.author.trim(), ignoreCase = true)
    }

@Composable
private fun SearchField(
    value: String,
    onValue: (String) -> Unit,
    placeholder: String,
    imeAction: ImeAction,
    onSearch: () -> Unit,
) {
    val palette = LocalAppPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.surfaceHigh, RoundedCornerShape(12.dp))
            .border(1.dp, palette.cardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) Text(placeholder, color = palette.fgMuted, fontSize = 15.sp)
            BasicTextField(
                value = value,
                onValueChange = onValue,
                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = imeAction),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ResultRow(r: BookSearchResult, isRead: Boolean, onClick: () -> Unit) {
    val palette = LocalAppPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 11.dp),
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
            if (isRead) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = CircleShape,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(10.dp),
                        )
                        Text(
                            "On your shelf",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
        if (isRead) {
            Icon(Icons.Filled.Check, contentDescription = "On your shelf", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        } else {
            Icon(Icons.Filled.Add, contentDescription = "Add", tint = palette.accentText, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun Hint(text: String) {
    val palette = LocalAppPalette.current
    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = palette.fgFaint, modifier = Modifier.size(26.dp))
        Text(text, color = palette.fgFaint, fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun NoMatches(onAddManual: () -> Unit) {
    val palette = LocalAppPalette.current
    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("No matches found.", color = palette.fgFaint, fontSize = 14.sp)
        Surface(
            color = palette.surfaceHigh,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(top = 14.dp).clickable { onAddManual() },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(15.dp))
                Text("Add it manually", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
