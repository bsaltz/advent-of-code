package com.github.bsaltz.adventofcode.y2023.d07

import com.github.bsaltz.adventofcode.util.Resource
import com.github.bsaltz.adventofcode.util.toClassPathResource
import com.github.bsaltz.adventofcode.util.toStringResource

private const val sampleResultP1: Int = 6440
private const val sampleResultP2: Int = 5905
private val sampleResource: Resource = """
    32T3K 765
    T55J5 684
    KK677 28
    KTJJT 220
    QQQJA 483
""".trimIndent().toStringResource()

private val inputResource: Resource = "2023/aoc-2023-d07-input.txt".toClassPathResource()

fun main() {
    println("# Part 1")
    println("  Sample result: ${Day7.solutionP1(sampleResource)} (expected: ${sampleResultP1})")
    println("  Input result: ${Day7.solutionP1(inputResource)}")
    println("# Part 2")
    println("  Sample result: ${Day7.solutionP2(sampleResource)} (expected: ${sampleResultP2})")
    println("  Input result: ${Day7.solutionP2(inputResource)}")
}

object Day7 {

    fun solutionP1(resource: Resource): Long = solution(resource, false)
    fun solutionP2(resource: Resource): Long = solution(resource, true)

    private fun solution(resource: Resource, jokers: Boolean): Long =
        resource.useLines { lines ->
            lines
                .filter { it.isNotBlank() }
                .map { parseHand(it, jokers) }
                .sorted()
                .mapIndexed { index, hand -> index + 1 to hand }
                .sumOf { (rank, hand) -> rank * hand.bid }
        }

    private fun parseHand(handString: String, jokers: Boolean): Hand =
        handString.split(Regex("\\s+")).let { (cardsString, bidString) ->
            Hand(parseCards(cardsString, jokers), bidString.toLong())
        }

    private fun parseCards(cardsString: String, jokers: Boolean): List<Card> = cardsString.map { Card.valueOf(it, jokers) }

    private fun handType(cards: List<Card>): HandType {
        val cardCounts = cardCounts(cards)
        val jokers = cardCounts[Card.JOKER] ?: 0
        val groupCounts = (cardCounts - Card.JOKER).values.toList()
        val maxCardCount = groupCounts.maxOrNull() ?: 0
        val groupCountsSize = groupCounts.size
        return when {
            maxCardCount == 5 -> HandType.FIVE_OF_A_KIND
            jokers >= 4 -> HandType.FIVE_OF_A_KIND
            maxCardCount + jokers == 5 -> HandType.FIVE_OF_A_KIND
            maxCardCount + jokers == 4 -> HandType.FOUR_OF_A_KIND
            groupCountsSize == 2 -> HandType.FULL_HOUSE
            maxCardCount + jokers == 3 -> HandType.THREE_OF_A_KIND
            groupCountsSize == 3 && jokers == 0 -> HandType.TWO_PAIR
            maxCardCount == 2 || jokers == 1 -> HandType.ONE_PAIR
            else -> HandType.HIGH_CARD
        }
    }

    private fun cardCounts(cards: List<Card>): Map<Card, Int> =
        cards.groupBy { it }.mapValues { (_, cards) -> cards.size }

    private data class Hand(
        val cards: List<Card>,
        val bid: Long,
        val type: HandType = handType(cards)
    ) : Comparable<Hand> {
        override fun compareTo(other: Hand): Int = comparator.compare(this, other)

        companion object {
            private val comparator: Comparator<Hand> =
                Comparator
                    .comparing<Hand, HandType> { it.type }
                    .thenComparing { h1, h2 ->
                        h1.cards.zip(h2.cards).asSequence()
                            .map { (c1, c2) -> c1.compareTo(c2) }
                            .first { it != 0 }
                    }
        }
    }

    private enum class Card(val char: Char) {
        JOKER('J'),
        TWO('2'),
        THREE('3'),
        FOUR('4'),
        FIVE('5'),
        SIX('6'),
        SEVEN('7'),
        EIGHT('8'),
        NINE('9'),
        TEN('T'),
        JACK('J'),
        QUEEN('Q'),
        KING('K'),
        ACE('A');

        companion object {
            fun valueOf(char: Char, jokers: Boolean): Card =
                entries
                    .filter {
                        when (it) {
                            JACK -> !jokers
                            JOKER -> jokers
                            else -> true
                        }
                    }
                    .firstOrNull { it.char == char }
                    ?: error("No ${Card::class.java.name} found for $char")

        }
    }

    private enum class HandType {
        HIGH_CARD, ONE_PAIR, TWO_PAIR, THREE_OF_A_KIND, FULL_HOUSE, FOUR_OF_A_KIND, FIVE_OF_A_KIND
    }
}
