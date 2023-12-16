package com.github.bsaltz.adventofcode.util

object StringUtils {
    val defaultTokenizeRegex = Regex("\\s+")

    inline fun <T> String.tokenize(splitRegex: Regex = defaultTokenizeRegex, transform: (String) -> T): List<T> =
        split(splitRegex).map(transform)

    fun String.tokenize(splitRegex: Regex = defaultTokenizeRegex): List<String> = tokenize(splitRegex) { it }
}
