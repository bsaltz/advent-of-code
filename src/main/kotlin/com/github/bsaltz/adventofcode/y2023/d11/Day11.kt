package com.github.bsaltz.adventofcode.y2023.d11

import com.github.bsaltz.adventofcode.util.Resource
import com.github.bsaltz.adventofcode.util.toClassPathResource
import com.github.bsaltz.adventofcode.util.toStringResource
import kotlin.math.abs

private const val sampleResultP1: Long = 374
private const val sampleResultP2: Long = 8410
private val sampleResource: Resource = """
    ...#......
    .......#..
    #.........
    ..........
    ......#...
    .#........
    .........#
    ..........
    .......#..
    #...#.....
""".trimIndent().toStringResource()

private val inputResource: Resource = "2023/aoc-2023-d11-input.txt".toClassPathResource()

fun main() {
    println("# Part 1")
    println("  Sample result 1: ${Day11.solutionP1(sampleResource)} (expected: ${sampleResultP1})")
    println("  Input result: ${Day11.solutionP1(inputResource)}")
    println("# Part 2")
    println("  Sample result 2: ${Day11.solutionP2(sampleResource, factor = 100L)} (expected: ${sampleResultP2})")
    println("  Input result: ${Day11.solutionP2(inputResource)}")
}

object Day11 {

    fun solutionP1(resource: Resource): Long = solution(resource)

    fun solutionP2(resource: Resource, factor: Long = 1000000L): Long = solution(resource, factor)

    private fun solution(resource: Resource, factor: Long = 2L) =
        resource.useLines { lines ->
            val galaxyMap = lines.foldIndexed(GalaxyMap()) { index, map, line ->
                map.copy(
                    width = line.length.toLong(),
                    height = index.toLong() + 1L,
                    galaxyCoordinates = map.galaxyCoordinates + parseCoordinates(index.toLong(), line)
                )
            }.expandSpace(factor)
            galaxyMap.galaxyCoordinates.mapIndexed { index, first ->
                galaxyMap.galaxyCoordinates.drop(index + 1).sumOf { second ->
                    abs(first.first - second.first) + abs(first.second - second.second)
                }
            }.sum()
        }

    private fun parseCoordinates(y: Long, line: String): List<Pair<Long, Long>> =
        line.mapIndexed { index, c -> index to c }
            .filter { (_, c) -> c == '#' }
            .map { (index, _) -> index.toLong() to y }

    private data class GalaxyMap(
        val width: Long = 0,
        val height: Long = 0,
        val galaxyCoordinates: List<Pair<Long, Long>> = emptyList(),
    ) {
        fun expandSpace(factor: Long = 2L): GalaxyMap {
            val emptyColumnIndices: List<Long> = (0..<width).filter { x -> galaxyCoordinates.none { it.first == x } }
            val emptyRowIndices: List<Long> = (0..<height).filter { y -> galaxyCoordinates.none { it.second == y } }
            val newGalaxyCoordinates = galaxyCoordinates.map { (x, y) ->
                x + emptyColumnIndices.count { it < x } * (factor - 1) to y + emptyRowIndices.count { it < y } * (factor - 1)
            }
            return GalaxyMap(
                width = width + emptyColumnIndices.size,
                height = height + emptyRowIndices.size,
                galaxyCoordinates = newGalaxyCoordinates,
            )
        }
    }
}
