package com.github.bsaltz.adventofcode.y2023.d16

import com.github.bsaltz.adventofcode.util.*
import com.github.bsaltz.adventofcode.util.IterableUtils.cartesianProduct

private const val sampleResultP1: Int = 46
private const val sampleResultP2: Int = 51
private val sampleResource: Resource = """
    .|...\....
    |.-.\.....
    .....|-...
    ........|.
    ..........
    .........\
    ..../.\\..
    .-.-/..|..
    .|....-|.\
    ..//.|....
""".trimIndent().toStringResource()

private val inputResource: Resource = "2023/aoc-2023-d16-input.txt".toClassPathResource()

fun main() {
    println("# Part 1")
    println("  Sample result 1: ${Day16.solutionP1(sampleResource)} (expected: ${sampleResultP1})")
    println("  Input result: ${Day16.solutionP1(inputResource)}")
    println("# Part 2")
    println("  Sample result 2: ${Day16.solutionP2(sampleResource)} (expected: ${sampleResultP2})")
    println("  Input result: ${Day16.solutionP2(inputResource)}")
}

object Day16 {

    fun solutionP1(resource: Resource): Int = resource.useLines(::parseGraph).energize().size

    fun solutionP2(resource: Resource): Int = resource.useLines(::parseGraph).let { graph ->
        val all = (0..<graph.width).cartesianProduct(listOf(Direction.NORTH, Direction.SOUTH)) +
            (0..<graph.height).cartesianProduct(listOf(Direction.EAST, Direction.WEST))
        all.maxOf { graph.energize(it.first, it.second).size }
    }

    private fun parseGraph(lines: Sequence<String>): Graph {
        val nodesByPoint: Map<Point, Node> = lines.flatMapIndexed { y, line ->
            line.mapIndexed { x, c -> Node(NodeType.valueOf(c), Point(x, y)) }
        }.associateBy { it.point }
        val width: Int = nodesByPoint.values.maxOf { it.point.x } + 1
        val height: Int = nodesByPoint.values.maxOf { it.point.y } + 1
        val newNodesByPoint: MutableMap<Point, Node> = nodesByPoint.toMutableMap()
        (0..<width).cartesianProduct(0..<height)
            .map { (x, y) -> Point(x, y) }
            .mapNotNull { newNodesByPoint[it] }
            .forEach { node ->
                val north = Edge(
                    nextPoint = connection(Direction.NORTH, node.point, width, height, newNodesByPoint),
                    nextDirection = Direction.NORTH,
                ).takeIf { it.nextPoint != node.point }
                val south = Edge(
                    nextPoint = connection(Direction.SOUTH, node.point, width, height, newNodesByPoint),
                    nextDirection = Direction.SOUTH,
                ).takeIf { it.nextPoint != node.point }
                val east = Edge(
                    nextPoint = connection(Direction.EAST, node.point, width, height, newNodesByPoint),
                    nextDirection = Direction.EAST,
                ).takeIf { it.nextPoint != node.point }
                val west = Edge(
                    nextPoint = connection(Direction.WEST, node.point, width, height, newNodesByPoint),
                    nextDirection = Direction.WEST,
                ).takeIf { it.nextPoint != node.point }
                val edges: Map<Direction, Set<Edge>> = when (node.type) {
                    // |
                    NodeType.SPLITTER_NS -> listOf(
                        Direction.NORTH to setOfNotNull(north),
                        Direction.SOUTH to setOfNotNull(south),
                        Direction.EAST to setOfNotNull(north, south),
                        Direction.WEST to setOfNotNull(north, south),
                    )
                    // -
                    NodeType.SPLITTER_EW -> listOf(
                        Direction.NORTH to setOfNotNull(east, west),
                        Direction.SOUTH to setOfNotNull(east, west),
                        Direction.EAST to setOfNotNull(east),
                        Direction.WEST to setOfNotNull(west),
                    )
                    // \
                    NodeType.MIRROR_EN -> listOf(
                        Direction.NORTH to setOfNotNull(west),
                        Direction.SOUTH to setOfNotNull(east),
                        Direction.EAST to setOfNotNull(south),
                        Direction.WEST to setOfNotNull(north),
                    )
                    // /
                    NodeType.MIRROR_WN -> listOf(
                        Direction.NORTH to setOfNotNull(east),
                        Direction.SOUTH to setOfNotNull(west),
                        Direction.EAST to setOfNotNull(north),
                        Direction.WEST to setOfNotNull(south),
                    )
                    else -> emptyList()
                }.toMap()
                newNodesByPoint[node.point] = node.copy(edgesByInputDirection = edges)
            }
        return Graph(newNodesByPoint, width, height)
    }

    private fun connection(
        direction: Direction,
        point: Point,
        width: Int,
        height: Int,
        newNodesByPoint: MutableMap<Point, Node>,
    ): Point {
        val a = when (direction) {
            Direction.NORTH, Direction.SOUTH -> point.x
            Direction.EAST, Direction.WEST -> point.y
        }
        val bRange = when (direction) {
            Direction.NORTH -> (0..<point.y).reversed()
            Direction.SOUTH -> point.y + 1..<height
            Direction.EAST -> point.x + 1..<width
            Direction.WEST -> (0..<point.x).reversed()
        }
        val maxB = when (direction) {
            Direction.NORTH -> 0
            Direction.SOUTH -> height - 1
            Direction.EAST -> width - 1
            Direction.WEST -> 0
        }
        val pointOf: (Int, Int) -> Point = { a, b ->
            when (direction) {
                Direction.NORTH, Direction.SOUTH -> Point(a, b)
                Direction.EAST, Direction.WEST -> Point(b, a)
            }
        }
        val b = bRange.firstOrNull { b ->
            val type = newNodesByPoint[pointOf(a, b)]?.type ?: NodeType.EMPTY
            type != NodeType.EMPTY
        } ?: maxB
        return pointOf(a, b)
    }

    private enum class Direction { NORTH, SOUTH, EAST, WEST }
    private enum class NodeType(val char: Char) {
        EMPTY('.'), SPLITTER_NS('|'), SPLITTER_EW('-'), MIRROR_WN('/'), MIRROR_EN('\\');
        companion object {
            fun valueOf(char: Char) = entries.first { it.char == char }
        }
    }
    private data class Point(val x: Int, val y: Int)
    private data class Node(
        val type: NodeType,
        val point: Point,
        val edgesByInputDirection: Map<Direction, Set<Edge>> = emptyMap(),
    )
    private data class Edge(val nextPoint: Point, val nextDirection: Direction)
    private data class Graph(val nodesByPoint: Map<Point, Node>, val width: Int, val height: Int) {
        fun firstNode(start: Int, direction: Direction): Node? {
            val a: (Point) -> Int = { (x, y) ->
                when (direction) {
                    Direction.NORTH, Direction.SOUTH -> y
                    Direction.EAST, Direction.WEST -> x
                }
            }
            val b: (Point) -> Int = { (x, y) ->
                when (direction) {
                    Direction.NORTH, Direction.SOUTH -> x
                    Direction.EAST, Direction.WEST -> y
                }
            }
            val nodes = nodesByPoint.values.filter { b(it.point) == start && it.type != NodeType.EMPTY }
            return when (direction) {
                Direction.NORTH, Direction.WEST -> nodes.maxByOrNull { a(it.point) }
                Direction.SOUTH, Direction.EAST -> nodes.minByOrNull { a(it.point) }
            }
        }

        override fun toString(): String =
            (0..<height).joinToString("\n") { y ->
                (0..<width).joinToString("") { x ->
                    "${nodesByPoint[Point(x, y)]?.type?.char ?: '.'}"
                }
            }

        fun energize(): Set<Point> = energize(0, Direction.EAST)

        fun energize(first: Int, firstDirection: Direction): Set<Point> {
            val firstNode: Node = firstNode(first, firstDirection) ?: return emptySet()
            val visited: MutableSet<Pair<Point, Direction>> = mutableSetOf()
            val energized: MutableSet<Point> = when (firstDirection) {
                Direction.NORTH -> listOf(first).cartesianProduct(firstNode.point.y..<height)
                Direction.SOUTH -> listOf(first).cartesianProduct(0..firstNode.point.y)
                Direction.EAST -> (0..firstNode.point.x).cartesianProduct(listOf(first))
                Direction.WEST -> (firstNode.point.x..<width).cartesianProduct(listOf(first))
            }.map { (x, y) -> Point(x, y) }.toMutableSet()
            val remaining: ArrayDeque<Pair<Node, Direction>> = ArrayDeque(listOf(firstNode to firstDirection))
            while (remaining.isNotEmpty()) {
                val (node, direction) = remaining.removeFirst()
                if (visited.contains(node.point to direction)) continue
                node.edgesByInputDirection[direction]?.forEach { (nextPoint, nextDirection) ->
                    val startX = minOf(node.point.x, nextPoint.x)
                    val startY = minOf(node.point.y, nextPoint.y)
                    val endX = maxOf(node.point.x, nextPoint.x)
                    val endY = maxOf(node.point.y, nextPoint.y)
                    val energizedPoints = (startX..endX).cartesianProduct(startY..endY)
                        .map { (x, y) -> Point(x, y) }
                    energized.addAll(energizedPoints)
                    remaining.addLast((nodesByPoint[nextPoint] ?: error("Missing node: $nextPoint")) to nextDirection)
                }
                visited.add(node.point to direction)
            }
            return energized
        }
    }
}