package com.github.bsaltz.adventofcode.y2023.d14

import com.github.bsaltz.adventofcode.util.*
import com.github.bsaltz.adventofcode.y2023.d14.Day14.DishMap.Companion.toDishMap

private const val sampleResultP1: Long = 136L
private const val sampleResultP2: Long = 64L
private val sampleResource: Resource = """
    O....#....
    O.OO#....#
    .....##...
    OO.#O....O
    .O.....O#.
    O.#..O.#.#
    ..O..#O..O
    .......O..
    #....###..
    #OO..#....
""".trimIndent().toStringResource()

private val inputResource: Resource = "2023/aoc-2023-d14-input.txt".toClassPathResource()

fun main() {
    println("# Part 1")
    println("  Sample result 1: ${Day14.solutionP1(sampleResource)} (expected: ${sampleResultP1})")
    println("  Input result: ${Day14.solutionP1(inputResource)}")
    println("# Part 2")
    println("  Sample result 2: ${Day14.solutionP2(sampleResource)} (expected: ${sampleResultP2})")
    println("  Input result: ${Day14.solutionP2(inputResource)}")
}

object Day14 {

    fun solutionP1(resource: Resource): Long =
        resource.useLines { lines -> parseLines(lines) }.tilt(Direction.NORTH).totalLoadNorth()

    fun solutionP2(resource: Resource): Long {
        // Approach: find the first loop in the tilt cycle and simulate its repetition until we reach 4 billion tilts
        val originalDishMap = resource.useLines { lines -> parseLines(lines) }

        // Store every occurrence of a tilt operation to find the loop effectively
        val tiltCountByTiltOperation: MutableMap<Pair<Direction, DishMap>, Long> = mutableMapOf()
        val dishMapByTiltCount: MutableMap<Long, DishMap> = mutableMapOf()

        // Find the end of the first loop
        val (loopEnd, loopDishMap) =
            generateSequence(1L to originalDishMap.tilt(Direction.NORTH)) { (tiltCount, currentDishMap) ->
                val nextDirection = nextTiltDirection(tiltCount)
                if (tiltCountByTiltOperation.contains(nextDirection to currentDishMap) || tiltCount > 4_000_000_000L) {
                    null
                } else {
                    tiltCountByTiltOperation[nextDirection to currentDishMap] = tiltCount
                    dishMapByTiltCount[tiltCount] = currentDishMap
                    tiltCount + 1 to currentDishMap.tilt(nextDirection)
                }
            }.last()
        // Find the start of the first loop
        val firstOccurrence = tiltCountByTiltOperation[nextTiltDirection(loopEnd) to loopDishMap] ?: error("No loop found")
        // Calculate the loop length
        val loopLength = loopEnd - firstOccurrence
        // Calculate the remaining loops in the sequence (int arithmetic will truncate decimals)
        val remainingLoops = (4_000_000_000L - loopEnd) / loopLength
        // Calculate how many tilt operations we'd have after the remaining loops
        val simulatedTiltCount = remainingLoops * loopLength + loopEnd
        // Calculate how many tilt operations would still be needed after the remaining loops
        val remainingTilts = 4_000_000_000L - simulatedTiltCount
        // Calculate the map's position within the original sequence based on the remaining tilt operations
        val simulatedRemainingTiltCount = firstOccurrence + remainingTilts
        // Simulate the tilts and return the solution
        return dishMapByTiltCount[simulatedRemainingTiltCount]?.totalLoadNorth()
            ?: error("Invalid state, no simulated dish map found")
    }

    private fun nextTiltDirection(tiltCount: Long) =
        when (tiltCount % 4L) {
            0L -> Direction.NORTH
            1L -> Direction.WEST
            2L -> Direction.SOUTH
            3L -> Direction.EAST
            else -> error("Unknown state: $tiltCount % 4 = ${tiltCount % 4L}")
        }

    private fun parseLines(lines: Sequence<String>): DishMap =
        lines.flatMapIndexed { y, line ->
            line.mapIndexed { x, c ->
                Point(x, y) to when (c) {
                    '.' -> EmptySpace
                    'O' -> RoundRock
                    '#' -> CubeRock
                    else -> error("Invalid character '$c'")
                }
            }
        }.toDishMap()

    private sealed class Space(val char: Char)
    private data object EmptySpace : Space('.')
    private data object RoundRock : Space('O')
    private data object CubeRock : Space('#')
    private data class Point(val x: Int, val y: Int)
    private enum class Direction { NORTH, SOUTH, EAST, WEST }

    private data class DishMap(
        val spacesByPoint: Map<Point, Space>,
        val width: Int,
        val height: Int,
    ) {
        private val xRange = 0..<width
        private val yRange = 0..<height

        fun space(x: Int, y: Int): Space =
            when {
                x !in xRange -> error("X out of bounds: $x >= $width")
                y !in yRange -> error("Y out of bounds: $y >= $width")
                else -> spacesByPoint[Point(x, y)] ?: EmptySpace
            }

        fun tilt(direction: Direction): DishMap {
            val (outerRange, innerRange) = when (direction) {
                Direction.NORTH -> xRange to yRange
                Direction.SOUTH -> xRange to yRange.reversed()
                Direction.EAST -> yRange to xRange.reversed()
                Direction.WEST -> yRange to xRange
            }
            val point: (Int, Int) -> Point = { a, b ->
                when (direction) {
                    Direction.NORTH -> Point(a, b)
                    Direction.SOUTH -> Point(a, b)
                    Direction.EAST -> Point(b, a)
                    Direction.WEST -> Point(b, a)
                }
            }
            val nextEmptyB: (Int) -> Int = {
                when (direction) {
                    Direction.NORTH, Direction.WEST -> it + 1
                    Direction.SOUTH, Direction.EAST -> it - 1
                }
            }

            val newSpacesByPoint = spacesByPoint.toMutableMap()
            outerRange.forEach { a ->
                var lastEmptyB: Int = -1
                innerRange.forEach { b ->
                    val space = newSpacesByPoint[point(a, b)] ?: EmptySpace
                    when (space) {
                        EmptySpace -> {
                            if (lastEmptyB == -1) {
                                lastEmptyB = b
                            }
                        }
                        CubeRock -> lastEmptyB = -1
                        RoundRock -> {
                            if (lastEmptyB != -1) {
                                newSpacesByPoint[point(a, lastEmptyB)] = RoundRock
                                newSpacesByPoint[point(a, b)] = EmptySpace
                                lastEmptyB = nextEmptyB(lastEmptyB)
                            }
                        }
                    }
                }
            }
            return valueOf(newSpacesByPoint)
        }

        fun totalLoadNorth(): Long =
            spacesByPoint.entries
                .filter { (_, space) -> space is RoundRock }
                .sumOf { (point, _) -> (height - point.y).toLong() }

        override fun toString(): String =
            yRange.joinToString("\n") { y -> xRange.joinToString("") { x -> space(x, y).char.toString() } }

        companion object {
            fun Sequence<Pair<Point, Space>>.toDishMap(): DishMap = valueOf(toMap())
            fun valueOf(spacesByPoint: Map<Point, Space>): DishMap =
                DishMap(spacesByPoint, spacesByPoint.keys.maxOf { it.x } + 1, spacesByPoint.keys.maxOf { it.y } + 1)
        }
    }
}