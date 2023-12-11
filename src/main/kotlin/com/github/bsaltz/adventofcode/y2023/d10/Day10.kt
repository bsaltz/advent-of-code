package com.github.bsaltz.adventofcode.y2023.d10

import com.github.bsaltz.adventofcode.util.Resource
import com.github.bsaltz.adventofcode.util.toClassPathResource
import com.github.bsaltz.adventofcode.util.toStringResource

private const val sampleResultP1: Int = 8
private val sampleResourceP1: Resource = """
    ..F7.
    .FJ|.
    SJ.L7
    |F--J
    LJ...
""".trimIndent().toStringResource()
private const val sampleResultP2: Int = 10
private val sampleResourceP2: Resource = """
    FF7FSF7F7F7F7F7F---7
    L|LJ||||||||||||F--J
    FL-7LJLJ||||||LJL-77
    F--JF--7||LJLJ7F7FJ-
    L---JF-JLJ.||-FJLJJ7
    |F|F-JF---7F7-L7L|7|
    |FFJF7L7F-JF7|JL---7
    7-L-JL7||F7|L7F-7F7|
    L.L7LFJ|||||FJL7||LJ
    L7JLJL-JLJLJL--JLJ.L
""".trimIndent().toStringResource()

private val inputResource: Resource = "2023/aoc-2023-d10-input.txt".toClassPathResource()

fun main() {
    println("# Part 1")
    println("  Sample result 1: ${Day10.solutionP1(sampleResourceP1)} (expected: ${sampleResultP1})")
    println("  Input result: ${Day10.solutionP1(inputResource)}")
    println("# Part 2")
    println("  Sample result 2: ${Day10.solutionP2(sampleResourceP2)} (expected: ${sampleResultP2})")
    println("  Input result: ${Day10.solutionP2(inputResource)}")
}

object Day10 {

    fun solutionP1(resource: Resource): Int =
        (resource.useLines { lines -> parseMap(lines) }.findLongestLoop().size - 1) / 2

    fun solutionP2(resource: Resource): Int =
        resource.useLines { lines -> parseMap(lines) }.let { it.countEnclosedTiles(it.findLongestLoop()) }

    private fun parseMap(lines: Sequence<String>): PipeMap =
        lines
            .mapIndexed { index, line -> index to line }
            .flatMap { (y, line) -> line.toCharArray().mapIndexed { x, c -> (x to y) to c } }
            .map { (coordinates, char) -> PipeSegment.of(coordinates, char) }
            .let { PipeMap.of(it.toList()) }

    private data class PipeMap(
        val segmentsByCoordinates: Map<Pair<Int, Int>, PipeSegment>,
        val size: Pair<Int, Int> = segmentsByCoordinates.values.first { it.startingPosition }.coordinates,
    ) {
        val startingPosition: Pair<Int, Int> = segmentsByCoordinates.values.first { it.startingPosition }.coordinates

        fun findLongestLoop(): List<PipeSegment> =
            startingPipeSegment().let { start ->
                generateSequence(listOf(start, connections(startingPosition).first())) { path ->
                    val (previous, current) = path.takeLast(2)
                    val next = connections(current.coordinates)
                        .filter { it != previous }
                        .also { check(it.size == 1) }
                        .first()
                    path + next
                }.dropWhile { it.last() != start }.first()
            }

        private fun startingPipeSegment(): PipeSegment = segmentsByCoordinates.values.first { it.startingPosition }

        private fun connections(coordinates: Pair<Int, Int>): List<PipeSegment> =
            segmentsByCoordinates[coordinates]?.let { segment ->
                segment.directions
                    .map { it to it.transformCoordinates(coordinates) }
                    .mapNotNull { (direction, adjacentCoordinates) ->
                        segmentsByCoordinates[adjacentCoordinates]?.let { direction to it }
                    }
                    .filter { (direction, adjacentSegment) ->
                        adjacentSegment.directions.contains(direction.opposite())
                    }
                    .map { it.second }
            }.orEmpty()

        fun countEnclosedTiles(loop: List<PipeSegment>): Int =
            of(loop, this.size).run {
                (0..<size.second).sumOf { y ->
                    (0..<size.first).fold(false to 0) { (inside, count), x ->
                        val current = segmentsByCoordinates[x to y]
                        val north = segmentsByCoordinates[x to y - 1]
                        val northFacing = current != null && current.directions.contains(PipeDirection.NORTH) &&
                                north != null && north.directions.contains(PipeDirection.SOUTH)
                        when {
                            northFacing -> !inside to count
                            inside && (current == null || current.directions.isEmpty()) -> inside to count + 1
                            else -> inside to count
                        }
                    }.second
                }
            }

        companion object {
            fun of(
                segments: Iterable<PipeSegment>,
                size: Pair<Int, Int> =
                    segments.map { it.coordinates }.reduce { (maxX, maxY), (x, y) -> maxOf(maxX, x) to maxOf(maxY, y) }
            ): PipeMap = PipeMap(segments.associateBy { it.coordinates }, size)
        }
    }

    private data class PipeSegment(
        val coordinates: Pair<Int, Int>,
        val directions: Set<PipeDirection>,
        val startingPosition: Boolean,
    ) {
        companion object {
            fun of(coordinates: Pair<Int, Int>, char: Char): PipeSegment =
                PipeSegment(
                    coordinates = coordinates,
                    directions = when (char) {
                        '|' -> setOf(PipeDirection.NORTH, PipeDirection.SOUTH)
                        '-' -> setOf(PipeDirection.EAST, PipeDirection.WEST)
                        'L' -> setOf(PipeDirection.NORTH, PipeDirection.EAST)
                        'J' -> setOf(PipeDirection.NORTH, PipeDirection.WEST)
                        '7' -> setOf(PipeDirection.SOUTH, PipeDirection.WEST)
                        'F' -> setOf(PipeDirection.SOUTH, PipeDirection.EAST)
                        '.' -> emptySet()
                        'S' -> PipeDirection.entries.toSet()
                        else -> error("Unknown pipe segment '$char'")
                    },
                    startingPosition = char == 'S',
                )
        }
    }

    private enum class PipeDirection {
        NORTH, SOUTH, EAST, WEST;

        fun opposite(): PipeDirection =
            when (this) {
                NORTH -> SOUTH
                SOUTH -> NORTH
                EAST -> WEST
                WEST -> EAST
            }

        fun transformCoordinates(coordinates: Pair<Int, Int>): Pair<Int, Int> =
            when (this) {
                NORTH -> coordinates.first to coordinates.second - 1
                SOUTH -> coordinates.first to coordinates.second + 1
                EAST -> coordinates.first + 1 to coordinates.second
                WEST -> coordinates.first - 1 to coordinates.second
            }
    }
}
