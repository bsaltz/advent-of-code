package com.github.bsaltz.adventofcode.util

object LangUtils {
    inline fun <T, K, V> Iterable<T>.associateIndexed(transform: (Int, T) -> Pair<K, V>): Map<K, V> =
        mapIndexed(transform).associate { it }

    fun <T> Sequence<T>.zipToPairs(): Sequence<Pair<T, T>> =
        zipWithNext().filterIndexed { index, _ -> index.and(1) == 0 }

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
}