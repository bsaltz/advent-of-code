package com.github.bsaltz.adventofcode.y2023.d04

import com.github.bsaltz.adventofcode.util.Resource
import com.github.bsaltz.adventofcode.util.toClassPathResource
import com.github.bsaltz.adventofcode.util.toStringResource

private const val sampleResultP1: Int = 13
private const val sampleResultP2: Int = 30
private val sampleResource: Resource = """
    Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53
    Card 2: 13 32 20 16 61 | 61 30 68 82 17 32 24 19
    Card 3:  1 21 53 59 44 | 69 82 63 72 16 21 14  1
    Card 4: 41 92 73 84 69 | 59 84 76 51 58  5 54 83
    Card 5: 87 83 26 28 32 | 88 30 70 12 93 22 82 36
    Card 6: 31 18 13 56 72 | 74 77 10 23 35 67 36 11
""".trimIndent().toStringResource()

private val inputResource: Resource = "2023/aoc-2023-d04-input.txt".toClassPathResource()

fun main() {
    println("# Part 1")
    println("  Sample result: ${Day4.solutionP1(sampleResource)} (expected: ${sampleResultP1})")
    println("  Input result: ${Day4.solutionP1(inputResource)}")
    println("# Part 2")
    println("  Sample result: ${Day4.solutionP2(sampleResource)} (expected: ${sampleResultP2})")
    println("  Input result: ${Day4.solutionP2(inputResource)}")
}

object Day4 {

    fun solutionP1(resource: Resource): Int = resource.useLines { it.sumOf(::points) }

    private fun points(cardString: String): Int = maxOf(1.shl(calculateMatches(cardString) - 1), 0)

    private fun calculateMatches(cardString: String): Int =
        cardString.split(':', '|').let { (_, winnersStr, presentStr) ->
            parseNumbers(winnersStr).intersect(parseNumbers(presentStr)).size
        }

    private fun parseNumbers(winnersStr: String) = winnersStr.trim().split(Regex("\\s+")).map { it.toInt() }.toSet()

    fun solutionP2(resource: Resource): Int =
        resource.useLines { lines ->
            lines.map { cardString -> Card(calculateMatches(cardString)) }.toList().let { cards ->
                cards.forEachIndexed { index, card ->
                    (1..card.matches).forEach { match -> cards[index + match].copies += card.copies }
                }
                cards.sumOf { it.copies }
            }
        }

    private data class Card(val matches: Int, var copies: Int = 1)
}
