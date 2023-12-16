package com.github.bsaltz.adventofcode.util

object IterableUtils {

    inline fun <T, K, V> Iterable<T>.associateIndexed(transform: (Int, T) -> Pair<K, V>): Map<K, V> =
        mapIndexed(transform).associate { it }

    inline fun <A> Iterable<A>.cartesianProduct(
        predicate: (Pair<A, A>) -> Boolean = { true }
    ): List<Pair<A, A>> =
        cartesianProduct(this, predicate)

    inline fun <A, B> Iterable<A>.cartesianProduct(
        other: Iterable<B>,
        predicate: (Pair<A, B>) -> Boolean = { true }
    ): List<Pair<A, B>> =
        cartesianProduct(other, predicate) { it }
    inline fun <A, B, R> Iterable<A>.cartesianProduct(
        other: Iterable<B>,
        predicate: (Pair<A, B>) -> Boolean = { true },
        transform: (Pair<A, B>) -> R,
    ): List<R> =
        flatMap { a -> other.map { b -> a to b }.filter(predicate).map(transform) }
}
