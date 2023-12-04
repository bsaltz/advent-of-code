package com.github.bsaltz.adventofcode.y2023.d01

import com.github.bsaltz.adventofcode.util.LangUtils.associateIndexed
import com.github.bsaltz.adventofcode.util.Resource
import com.github.bsaltz.adventofcode.util.toClassPathResource
import com.github.bsaltz.adventofcode.util.toStringResource

private const val sampleResultP1: Int = 142
private val sampleResourceP1: Resource = """
    1abc2
    pqr3stu8vwx
    a1b2c3d4e5f
    treb7uchet
""".trimIndent().toStringResource()

private const val sampleResultP2: Int = 281
private val sampleResourceP2: Resource = """
    two1nine
    eightwothree
    abcone2threexyz
    xtwone3four
    4nineeightseven2
    zoneight234
    7pqrstsixteen
""".trimIndent().toStringResource()

private val inputResource: Resource = "2023/aoc-2023-d01-input.txt".toClassPathResource()

fun main() {
    println("# Part 1")
    println("  Sample result: ${Day1.solutionP1(sampleResourceP1)} (expected: $sampleResultP1)")
    println("  Input result: ${Day1.solutionP1(inputResource)}")
    println("# Part 2")
    println("  Sample result: ${Day1.solutionP2(sampleResourceP2)} (expected: $sampleResultP2)")
    println("  Input result: ${Day1.solutionP2(inputResource)}")
}

object Day1 {

    private val digitStringMap = (1..9).associateBy { it.toString() }
    private val wordStringMap =
        listOf("one", "two", "three", "four", "five", "six", "seven", "eight", "nine")
            .associateIndexed { i, s -> s to i + 1 }

    private val stringValueMap: Map<String, Int> = digitStringMap + wordStringMap
    private val maxCheckLength: Int = stringValueMap.keys.maxOf { it.length }

    fun solutionP1(input: Resource): Int = input.useLines { lines -> lines.calibrationValueP1() }

    private fun Sequence<String>.calibrationValueP1(): Int =
        sumOf { line -> line.first { it.isDigit() }.digitToInt() * 10 + line.last { it.isDigit() }.digitToInt() }

    fun solutionP2(input: Resource): Int =
        input.useLines { lines -> lines.sumOf { it.firstDigit() * 10 + it.lastDigit() } }

    private fun String.firstDigit(): Int =
        indices.asSequence()
            .map { substring(it..<minOf(it + maxCheckLength, length)) }
            .firstNotNullOf { check ->
                stringValueMap.entries.firstOrNull { (string, _) -> check.startsWith(string) }?.value
            }

    private fun String.lastDigit(): Int =
        indices.reversed().asSequence()
            .map { substring(it..<minOf(it + maxCheckLength, length)) }
            .firstNotNullOf { check ->
                stringValueMap.entries.firstOrNull { (string, _) -> check.startsWith(string) }?.value
            }
}
