package com.github.bsaltz.adventofcode.util

import java.lang.ref.WeakReference
import java.util.Collections

object Memoization {
    private val references: MutableList<WeakReference<Memoize>> = Collections.synchronizedList(mutableListOf())

    fun stats(): List<MemoStats> = withReferences { it.toList() }.mapNotNull { it.get()?.stats() }
    fun register(memoize: Memoize) = withReferences { it.add(WeakReference(memoize)) }

    private inline fun <R> withReferences(block: (MutableList<WeakReference<Memoize>>) -> R): R =
        synchronized(references) { block(getReferences()) }

    private fun getReferences(): MutableList<WeakReference<Memoize>> =
        references.also { it.removeAll { reference -> reference.get() == null } }
}

data class MemoStats(
    val cacheName: String?,
    val cacheSize: Int,
)

interface Memoize {
    val cacheName: String?
    fun cacheSize(): Int
    fun stats(): MemoStats = MemoStats(cacheName, cacheSize())
}

class Memoize1<in A, in K, out R>(
    override val cacheName: String?,
    private val f: (A) -> R,
    private val keyProvider: (A) -> K,
) : Memoize, (A) -> R {
    private val cache: MutableMap<K, R> = mutableMapOf()
    override fun cacheSize(): Int = cache.size
    override fun invoke(p1: A): R = cache.getOrPut(keyProvider(p1)) { f(p1) }
}

class Memoize2<in A, in B, in K, out R>(
    override val cacheName: String?,
    private val f: (A, B) -> R,
    private val keyProvider: (A, B) -> K,
) : Memoize, (A, B) -> R {
    private val cache: MutableMap<K, R> = mutableMapOf()
    override fun cacheSize(): Int = cache.size
    override fun invoke(p1: A, p2: B): R = cache.getOrPut(keyProvider(p1, p2)) { f(p1, p2) }
}

class Memoize3<in A, in B, in C, in K, out R>(
    override val cacheName: String?,
    private val f: (A, B, C) -> R,
    private val keyProvider: (A, B, C) -> K,
) : Memoize, (A, B, C) -> R {
    private val cache: MutableMap<K, R> = mutableMapOf()
    override fun cacheSize(): Int = cache.size
    override fun invoke(p1: A, p2: B, p3: C): R = cache.getOrPut(keyProvider(p1, p2, p3)) { f(p1, p2, p3) }
}

class Memoize4<in A, in B, in C, in D, in K, out R>(
    override val cacheName: String?,
    private val f: (A, B, C, D) -> R,
    private val keyProvider: (A, B, C, D) -> K,
) : Memoize, (A, B, C, D) -> R {
    private val cache: MutableMap<K, R> = mutableMapOf()
    override fun cacheSize(): Int = cache.size
    override fun invoke(p1: A, p2: B, p3: C, p4: D): R =
        cache.getOrPut(keyProvider(p1, p2, p3, p4)) { f(p1, p2, p3, p4) }
}

class Memoize5<in A, in B, in C, in D, in E, in K, out R>(
    override val cacheName: String?,
    private val f: (A, B, C, D, E) -> R,
    private val keyProvider: (A, B, C, D, E) -> K,
) : Memoize, (A, B, C, D, E) -> R {
    private val cache: MutableMap<K, R> = mutableMapOf()
    override fun cacheSize(): Int = cache.size
    override fun invoke(p1: A, p2: B, p3: C, p4: D, p5: E): R =
        cache.getOrPut(keyProvider(p1, p2, p3, p4, p5)) { f(p1, p2, p3, p4, p5) }
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
data class Quint<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)

fun <A, R> ((A) -> R).memoize(cacheName: String? = null): (A) -> R =
    memoizeOn(cacheName) { it }
fun <A, B, R> ((A, B) -> R).memoize(cacheName: String? = null): (A, B) -> R =
    memoizeOn(cacheName, ::Pair)
fun <A, B, C, R> ((A, B, C) -> R).memoize(cacheName: String? = null): (A, B, C) -> R =
    memoizeOn(cacheName, ::Triple)
fun <A, B, C, D, R> ((A, B, C, D) -> R).memoize(cacheName: String? = null): (A, B, C, D) -> R =
    memoizeOn(cacheName, ::Quad)
fun <A, B, C, D, E, R> ((A, B, C, D, E) -> R).memoize(cacheName: String? = null): (A, B, C, D, E) -> R =
    memoizeOn(cacheName, ::Quint)

fun <A, K, R> ((A) -> R).memoizeOn(
    cacheName: String? = null,
    keyProvider: (A) -> K
): (A) -> R =
    Memoize1(cacheName, this, keyProvider).also { Memoization.register(it) }

fun <A, B, K, R> ((A, B) -> R).memoizeOn(
    cacheName: String? = null,
    keyProvider: (A, B) -> K
): (A, B) -> R =
    Memoize2(cacheName, this, keyProvider).also { Memoization.register(it) }

fun <A, B, C, K, R> ((A, B, C) -> R).memoizeOn(
    cacheName: String? = null,
    keyProvider: (A, B, C) -> K
): (A, B, C) -> R =
    Memoize3(cacheName, this, keyProvider).also { Memoization.register(it) }

fun <A, B, C, D, K, R> ((A, B, C, D) -> R).memoizeOn(
    cacheName: String? = null,
    keyProvider: (A, B, C, D) -> K
): (A, B, C, D) -> R =
    Memoize4(cacheName, this, keyProvider).also { Memoization.register(it) }

fun <A, B, C, D, E, K, R> ((A, B, C, D, E) -> R).memoizeOn(
    cacheName: String? = null,
    keyProvider: (A, B, C, D, E) -> K
): (A, B, C, D, E) -> R =
    Memoize5(cacheName, this, keyProvider).also { Memoization.register(it) }
