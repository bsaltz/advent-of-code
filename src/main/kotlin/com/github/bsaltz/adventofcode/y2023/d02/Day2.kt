package com.github.bsaltz.adventofcode.y2023.d02

import com.github.bsaltz.adventofcode.util.Resource
import com.github.bsaltz.adventofcode.util.toClassPathResource
import com.github.bsaltz.adventofcode.util.toStringResource

private const val sampleResultP1: Int = 8
private const val sampleResultP2: Int = 2286
private val sampleResource: Resource = """
    Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
    Game 2: 1 blue, 2 green; 3 green, 4 blue, 1 red; 1 green, 1 blue
    Game 3: 8 green, 6 blue, 20 red; 5 blue, 4 red, 13 green; 5 green, 1 red
    Game 4: 1 green, 3 red, 6 blue; 3 green, 6 red; 3 green, 15 blue, 14 red
    Game 5: 6 red, 1 blue, 3 green; 2 blue, 1 red, 2 green
""".trimIndent().toStringResource()

private val inputResource: Resource = "2023/aoc-2023-d02-input.txt".toClassPathResource()

fun main() {
    println("# Part 1")
    println("  Sample result: ${Day2.solutionP1(sampleResource)} (expected: $sampleResultP1)")
    println("  Input result: ${Day2.solutionP1(inputResource)}")
    println("# Part 2")
    println("  Sample result: ${Day2.solutionP2(sampleResource)} (expected: $sampleResultP2)")
    println("  Input result: ${Day2.solutionP2(inputResource)}")
}

object Day2 {
    private const val MAX_RED: Int = 12
    private const val MAX_GREEN: Int = 13
    private const val MAX_BLUE: Int = 14

    private val gameRegex: Regex = Regex("Game (\\d+): (.*)")
    private val revealSplitRegex: Regex = Regex("; ")
    private val revealRegex: Regex = Regex("(\\d+) (red|green|blue)")

    fun solutionP1(input: Resource): Int =
        input.useLines { it.map(Day2::parseGame).filter(Game::possible).sumOf(Game::id) }

    fun solutionP2(input: Resource): Int =
        input.useLines { it.map(Day2::parseGame).sumOf(Game::power) }

    private fun parseGame(gameString: String): Game =
        gameRegex.matchEntire(gameString)?.let {
            Game(
                id = it.groupValues[1].toInt(),
                reveals = it.groupValues[2].split(revealSplitRegex).map(Day2::parseReveal)
            )
        } ?: error("No match found: '$gameString'")

    private fun parseReveal(revealString: String): Reveal =
        revealRegex.findAll(revealString)
            .associate { it.groupValues[2] to it.groupValues[1].toInt() }
            .let { Reveal(it["red"] ?: 0, it["green"] ?: 0, it["blue"] ?: 0) }

    private data class Game(val id: Int, val reveals: List<Reveal>) {
        val possible: Boolean by lazy { reveals.all(Reveal::possible) }
        val power: Int by lazy { reveals.run { maxOf { it.red } * maxOf { it.green } * maxOf { it.blue } } }
    }

    private data class Reveal(val red: Int, val green: Int, val blue: Int) {
        val possible: Boolean by lazy { red <= MAX_RED && green <= MAX_GREEN && blue <= MAX_BLUE }
    }
}
