package com.github.bsaltz.adventofcode.util

object LangUtils {
    inline fun <T, K, V> Iterable<T>.associateIndexed(transform: (Int, T) -> Pair<K, V>): Map<K, V> =
        mapIndexed(transform).associate { it }
}