package com.github.bsaltz.adventofcode.y2023.d13

import com.github.bsaltz.adventofcode.util.*
import com.github.bsaltz.adventofcode.util.IterableUtils.cartesianProduct
import com.github.bsaltz.adventofcode.util.SequenceUtils.chunked

private const val sampleResultP1: Long = 405L
private const val sampleResultP2: Long = 400L
private val sampleResource: Resource = """
    #.##..##.
    ..#.##.#.
    ##......#
    ##......#
    ..#.##.#.
    ..##..##.
    #.#.##.#.
    
    #...##..#
    #....#..#
    ..##..###
    #####.##.
    #####.##.
    ..##..###
    #....#..#
""".trimIndent().toStringResource()

private val inputResource: Resource = "2023/aoc-2023-d13-input.txt".toClassPathResource()

fun main() {
    println("# Part 1")
    println("  Sample result 1: ${Day13.solutionP1(sampleResource)} (expected: ${sampleResultP1})")
    println("  Input result: ${Day13.solutionP1(inputResource)}")
    println("# Part 2")
    println("  Sample result 2: ${Day13.solutionP2(sampleResource)} (expected: ${sampleResultP2})")
    println("  Input result: ${Day13.solutionP2(inputResource)}")
}

object Day13 {

    fun solutionP1(resource: Resource): Long = resource.useLines { lines ->
        parseLines(lines)
            .map { mirrorLines(it) }
            .sumOf { mirrorSummary(it).toLong() }
    }

    fun solutionP2(resource: Resource): Long = resource.useLines { lines ->
        parseLines(lines)
            .map { it to mirrorLines(it) }
            .map { (lines, mirrorLines) ->
                (0..<lines.first().length)
                    .cartesianProduct(other = lines.indices)
                    .asSequence()
                    .map { (columnIndex, rowIndex) -> smudge(lines, columnIndex, rowIndex) }
                    .map { smudgeLines -> mirrorLines(smudgeLines).filter { it !in mirrorLines } }
                    .firstOrNull { it.isNotEmpty() }
                    ?: error("No new lines found with smudges:\n${lines.joinToString("\n")}")
            }
            .sumOf { mirrorSummary(it).toLong() }
    }

    private fun parseLines(lines: Sequence<String>) =
        lines.chunked { it.isBlank() }.map { mirrorLines -> mirrorLines.filter { it.isNotBlank() } }

    private fun mirrorSummary(mirrorLines: List<MirrorLine>): Int =
        (mirrorLines.firstOrNull { it is VerticalMirrorLine }?.index ?: 0) +
            (mirrorLines.firstOrNull { it is HorizontalMirrorLine }?.index ?: 0) * 100

    private fun mirrorLines(lines: List<String>): List<MirrorLine> =
        horizontalMirrorLineIndices(transposeLines(lines)).map { VerticalMirrorLine(it) } +
            horizontalMirrorLineIndices(lines).map { HorizontalMirrorLine(it) }

    private fun horizontalMirrorLineIndices(lines: List<String>): List<Int> =
        lines
            .mapIndexed { index, line -> index to line }
            .groupBy { (_, line) -> line }
            .values
            .filter { it.size > 1 }
            .flatMap { it.toCandidatePairs() }
            .filter { it.validCandidate(lines) }
            .map { it.second.index }

    private fun List<Pair<Int, String>>.toCandidatePairs(): List<Pair<Candidate, Candidate>> =
        cartesianProduct { (a, b) -> a != b }
            .map { (a, b) -> a.toCandidate() to b.toCandidate() }
            .filter { (a, b) -> b.index - a.index == 1 }

    private fun Pair<Int, String>.toCandidate(): Candidate = Candidate(first, second)

    private fun Pair<Candidate, Candidate>.validCandidate(lines: List<String>): Boolean =
        let { (a, b) ->
            generateSequence(a.index to b.index) { (i1, i2) -> i1 - 1 to i2 + 1 }
                .takeWhile { (i1, i2) -> i1 >= 0 && i2 < lines.size }
                .map { (i1, i2) -> lines[i1] to lines[i2] }
                .all { (s1, s2) -> s1 == s2 }
        }

    private fun transposeLines(lines: List<String>): List<String> =
        List(lines.first().length) { column -> lines.map { it[column] }.joinToString("") }

    private fun smudge(lines: List<String>, smudgeColumnIndex: Int, smudgeRowIndex: Int): List<String> =
        lines.mapIndexed { index, line ->
            if (index == smudgeRowIndex) {
                val newChar = when (line[smudgeColumnIndex]) {
                    '#' -> '.'
                    '.' -> '#'
                    else -> error("Invalid character '${line[smudgeColumnIndex]}'")
                }
                "${line.substring(0, smudgeColumnIndex)}$newChar${line.substring(smudgeColumnIndex + 1, line.length)}"
            } else {
                lines[index]
            }
        }

    private data class Candidate(val index: Int, val line: String)

    private sealed interface MirrorLine {
        val index: Int
    }
    private data class VerticalMirrorLine(override val index: Int) : MirrorLine
    private data class HorizontalMirrorLine(override val index: Int) : MirrorLine
}
