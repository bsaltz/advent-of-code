package com.github.bsaltz.adventofcode.y2023.d03

import com.github.bsaltz.adventofcode.util.Resource
import com.github.bsaltz.adventofcode.util.toClassPathResource
import com.github.bsaltz.adventofcode.util.toStringResource

private const val sampleResultP1: Int = 4361
private const val sampleResultP2: Int = 467835
private val sampleResource: Resource = """
    467..114..
    ...*......
    ..35..633.
    ......#...
    617*......
    .....+.58.
    ..592.....
    ......755.
    ...$.*....
    .664.598..
""".trimIndent().toStringResource()

private val inputResource: Resource = "2023/aoc-2023-d03-input.txt".toClassPathResource()

fun main() {
    println("# Part 1")
    println("  Sample result: ${Day3.solutionP1(sampleResource)} (expected: ${sampleResultP1})")
    println("  Input result: ${Day3.solutionP1(inputResource)}")
    println("# Part 2")
    println("  Sample result: ${Day3.solutionP2(sampleResource)} (expected: ${sampleResultP2})")
    println("  Input result: ${Day3.solutionP2(inputResource)}")
}

object Day3 {

    fun solutionP1(resource: Resource): Int =
        resource.useLines { sequence ->
            val lines = sequence.toList()
            lines.indices.sumOf { lineNumber ->
                val curr = lines[lineNumber]
                val prev = lines.getOrNull(lineNumber - 1)
                val next = lines.getOrNull(lineNumber + 1)

                val symbolBitmap = symbolBitmap(prev, curr, next)

                var sum = 0
                var value = 0
                var include = false
                for (i in curr.indices) {
                    val c = curr[i]
                    if (c.isDigit()) {
                        value = value * 10 + c.digitToInt()
                        include = include ||
                            symbolBitmap.getOrElse(i - 1) { false } ||
                            symbolBitmap.getOrElse(i) { false } ||
                            symbolBitmap.getOrElse(i + 1) { false }
                    } else {
                        if (value > 0 && include) {
                            sum += value
                        }
                        value = 0
                        include = false
                    }
                }
                if (value > 0 && include) {
                    sum += value
                }
                sum
            }
        }

    private fun symbolBitmap(vararg strings: String?): List<Boolean> =
        strings.filterNotNull().let { notNull ->
            if (notNull.isNotEmpty()) {
                notNull.fold(List(notNull.first().length) { false }) { acc, s -> acc.orAll(s.symbolBitmap()) }
            } else {
                emptyList()
            }
        }

    private fun String.symbolBitmap(): List<Boolean> = toCharArray().map { !it.isDigit() && it != '.' }

    private fun List<Boolean>.orAll(list: List<Boolean>): List<Boolean> {
        check(size == list.size)
        return zip(list).map { it.first || it.second }
    }

    fun solutionP2(resource: Resource): Int =
        resource.useLines { sequence ->
            val lines = sequence.toList()
            lines.indices.sumOf { lineNumber ->
                val curr = lines[lineNumber]
                val prev = lines.getOrNull(lineNumber - 1)
                val next = lines.getOrNull(lineNumber + 1)

                val gearBitmap = curr.gearBitmap()
                val prevNumberMap = prev?.numberMap()
                val currNumberMap = curr.numberMap()
                val nextNumberMap = next?.numberMap()

                gearBitmap.mapIndexed { i, gear ->
                    if (gear) {
                        val allNumbers = listOfNotNull(
                            prevNumberMap.findGearNumbers(i),
                            currNumberMap.findGearNumbers(i),
                            nextNumberMap.findGearNumbers(i)
                        ).flatten()
                        if (allNumbers.size == 2) {
                            return@mapIndexed allNumbers[0] * allNumbers[1]
                        }
                    }
                    return@mapIndexed 0
                }.sum()
            }
        }

    private fun String.gearBitmap(): List<Boolean> = toCharArray().map { it == '*' }
    private fun String.numberMap(): List<Int?> =
        MutableList<Int?>(length) { null }.also { result ->
            var value = 0
            var start = -1
            var end = -1
            for (i in indices) {
                val c = get(i)
                if (c.isDigit()) {
                    if (start < 0) {
                        value = c.digitToInt()
                        start = i
                    } else {
                        value = value * 10 + c.digitToInt()
                    }
                    end = i
                } else {
                    if (start >= 0) {
                        (start..end).forEach { result[it] = value }
                    }
                    value = 0
                    start = -1
                    end = -1
                }
            }
            if (start >= 0) {
                (start..end).forEach { result[it] = value }
            }
        }
    private fun List<Int?>?.findGearNumbers(gearIndex: Int): List<Int> =
        this?.run {
            if (this[gearIndex] == null) {
                listOfNotNull(this.getOrNull(gearIndex - 1), this.getOrNull(gearIndex + 1))
            } else {
                listOfNotNull(this[gearIndex])
            }
        }.orEmpty()
}
