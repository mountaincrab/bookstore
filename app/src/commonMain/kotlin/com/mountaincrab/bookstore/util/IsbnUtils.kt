package com.mountaincrab.bookstore.util

/**
 * Normalise any raw ISBN string (with or without hyphens/spaces) to a 13-digit
 * ISBN-13 string, or return null if the input isn't a valid ISBN-10 or ISBN-13.
 *
 * ISBN-10 → ISBN-13: prefix "978", drop the old check digit, recompute.
 * ISBN-13 starting with "979": stored as-is (no ISBN-10 equivalent exists).
 */
fun normaliseIsbn(raw: String): String? {
    val digits = raw.filter { it.isDigit() || it == 'X' || it == 'x' }
    return when (digits.length) {
        13 -> if (digits.all { it.isDigit() }) digits else null
        10 -> isbn10ToIsbn13(digits)
        else -> null
    }
}

private fun isbn10ToIsbn13(isbn10: String): String? {
    val body = isbn10.take(9)
    if (!body.all { it.isDigit() }) return null
    val prefix = "978$body"
    val check = isbn13CheckDigit(prefix)
    return "$prefix$check"
}

private fun isbn13CheckDigit(first12: String): Int {
    val sum = first12.mapIndexed { i, c -> c.digitToInt() * if (i % 2 == 0) 1 else 3 }.sum()
    return (10 - sum % 10) % 10
}
