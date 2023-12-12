package com.github.bsaltz.adventofcode.util

object LangUtils {
    val defaultTokenizeRegex = Regex("\\s+")

    inline fun <T, K, V> Iterable<T>.associateIndexed(transform: (Int, T) -> Pair<K, V>): Map<K, V> =
        mapIndexed(transform).associate { it }

    fun <T> Sequence<T>.zipToPairs(): Sequence<Pair<T, T>> = chunked(2) { (first, second) -> first to second }

    fun LongRange.intersectRange(other: LongRange): LongRange? {
        if (step != 1L || other.step != 1L) {
            TODO("This operation currently only supports ranges with a step value of 1,")
        }
        val thisFirst = first
        val thisLast = last
        val otherLast = other.last
        val otherFirst = other.first
        return if (thisFirst <= otherLast && otherFirst <= thisLast) {
            maxOf(thisFirst, otherFirst)..minOf(thisLast, otherLast)
        } else {
            null
        }
    }

    fun LongRange.shiftRange(distance: Long): LongRange = LongRange(start + distance, endInclusive + distance)

    inline fun <T> String.tokenize(splitRegex: Regex = defaultTokenizeRegex, transform: (String) -> T): List<T> =
        split(splitRegex).map(transform)

    fun String.tokenize(splitRegex: Regex = defaultTokenizeRegex): List<String> = tokenize(splitRegex) { it }
}