package com.mountaincrab.bookstore.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountaincrab.bookstore.data.local.entity.BookEntity
import com.mountaincrab.bookstore.data.remote.BookSearchResult
import com.mountaincrab.bookstore.ui.theme.LocalAppPalette

/**
 * The add/edit form. Three entry points:
 *  - blank manual add (`existing` and `seed` both null),
 *  - add pre-filled from an online search result (`seed` set), and
 *  - edit an existing book (`existing` set).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBookSheet(
    existing: BookEntity?,
    seed: BookSearchResult?,
    onDismiss: () -> Unit,
    viewModel: AddEditBookViewModel,
) {
    val palette = LocalAppPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEdit = existing != null

    var title by remember { mutableStateOf(existing?.title ?: seed?.title ?: "") }
    var author by remember { mutableStateOf(existing?.author ?: seed?.author ?: "") }
    var genres by remember {
        mutableStateOf((existing?.genres ?: seed?.genres ?: emptyList()).joinToString(", "))
    }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    fun save() {
        if (title.isBlank()) return
        val genreList = genres.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (existing != null) {
            viewModel.updateBook(
                existing.copy(
                    title = title.trim(),
                    author = author.trim().ifEmpty { "Unknown" },
                    genres = genreList,
                    notes = notes.trim(),
                )
            )
        } else {
            viewModel.addBook(title, author, genreList, notes)
        }
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                    if (isEdit) "Edit book" else "Add details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Box(
                    modifier = Modifier.size(32.dp).background(palette.surfaceHigh, RoundedCornerShape(50)).clickable { onDismiss() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = palette.fgMuted, modifier = Modifier.size(18.dp))
                }
            }

            Column(
                modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LabeledField("TITLE") { FormInput(title, { title = it }, "Book title") }
                LabeledField("AUTHOR") { FormInput(author, { author = it }, "Author name") }
                LabeledField("GENRES") { FormInput(genres, { genres = it }, "Fiction, Sci-fi…") }
                LabeledField("NOTES") { FormInput(notes, { notes = it }, "What did you think?", minLines = 3) }

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().clickable { save() },
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
