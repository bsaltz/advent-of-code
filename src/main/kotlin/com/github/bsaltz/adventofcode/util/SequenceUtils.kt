package com.github.bsaltz.adventofcode.util

object SequenceUtils {

    fun <T> Sequence<T>.zipToPairs(): Sequence<Pair<T, T>> = chunked(2) { (first, second) -> first to second }

    fun <T> Sequence<T>.chunked(predicate: (T) -> Boolean): Sequence<List<T>> = chunkedInternal(this, predicate)

    private inline fun <T> chunkedInternal(
        sequence: Sequence<T>,
        crossinline predicate: (T) -> Boolean
    ): Sequence<List<T>> =
        sequence {
            val accumulator: MutableList<T> = mutableListOf()
            sequence.forEach { t ->
                accumulator += t
                if (predicate(t)) {
                    yield(accumulator)
                    accumulator.clear()
                }
            }
            accumulator.takeIf { it.isNotEmpty() }?.also { yield(it) }
        }
}
