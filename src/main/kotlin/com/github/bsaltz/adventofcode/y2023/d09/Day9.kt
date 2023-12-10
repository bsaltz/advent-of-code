package com.github.bsaltz.adventofcode.y2023.d09

import com.github.bsaltz.adventofcode.util.LangUtils.tokenize
import com.github.bsaltz.adventofcode.util.Resource
import com.github.bsaltz.adventofcode.util.toClassPathResource
import com.github.bsaltz.adventofcode.util.toStringResource

private const val sampleResultP1: Int = 114
private const val sampleResultP2: Int = 2
private val sampleResource: Resource = """
    0 3 6 9 12 15
    1 3 6 10 15 21
    10 13 16 21 30 45
""".trimIndent().toStringResource()

private val inputResource: Resource = "2023/aoc-2023-d09-input.txt".toClassPathResource()

fun main() {
    println("# Part 1")
    println("  Sample result 1: ${Day9.solutionP1(sampleResource)} (expected: ${sampleResultP1})")
    println("  Input result: ${Day9.solutionP1(inputResource)}")
    println("# Part 2")
    println("  Sample result 2: ${Day9.solutionP2(sampleResource)} (expected: ${sampleResultP2})")
    println("  Input result: ${Day9.solutionP2(inputResource)}")
}

object Day9 {

    fun solutionP1(resource: Resource): Long =
        resource.useLines { lines ->
            lines.sumOf { line ->
                generateSequence(parseLine(line)) { list -> mapDown(list) }
                    .takeWhile { list -> !list.all { it == 0L } }
                    .sumOf { it.last() }
            }
        }

    fun solutionP2(resource: Resource): Long =
        resource.useLines { lines ->
            lines.sumOf { line ->
                generateSequence(parseLine(line)) { list -> mapDown(list) }
                    .takeWhile { list -> !list.all { it == 0L } }
                    .map { it.first() }
                    .toList()
                    .reversed()
                    .fold(0L) { acc, value -> value - acc }
            }
        }

    private fun parseLine(line: String) = line.tokenize { it.toLong() }
    private fun mapDown(line: List<Long>) = line.zipWithNext().map { it.second - it.first }
}
