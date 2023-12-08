package com.github.bsaltz.adventofcode.y2023.d08

import com.github.bsaltz.adventofcode.util.Resource
import com.github.bsaltz.adventofcode.util.toClassPathResource
import com.github.bsaltz.adventofcode.util.toStringResource

private const val sampleResult1P1: Int = 2
private const val sampleResult2P1: Int = 6
private val sampleResource1P1: Resource = """
    RL
    
    AAA = (BBB, CCC)
    BBB = (DDD, EEE)
    CCC = (ZZZ, GGG)
    DDD = (DDD, DDD)
    EEE = (EEE, EEE)
    GGG = (GGG, GGG)
    ZZZ = (ZZZ, ZZZ)
""".trimIndent().toStringResource()
private val sampleResource2P1: Resource = """
    LLR
    
    AAA = (BBB, BBB)
    BBB = (AAA, ZZZ)
    ZZZ = (ZZZ, ZZZ)
""".trimIndent().toStringResource()

private const val sampleResultP2: Int = 6
private val sampleResourceP2: Resource = """
    LR
    
    11A = (11B, XXX)
    11B = (XXX, 11Z)
    11Z = (11B, XXX)
    22A = (22B, XXX)
    22B = (22C, 22C)
    22C = (22Z, 22Z)
    22Z = (22B, 22B)
    XXX = (XXX, XXX)
""".trimIndent().toStringResource()

private val inputResource: Resource = "2023/aoc-2023-d08-input.txt".toClassPathResource()

fun main() {
    println("# Part 1")
    println("  Sample result 1: ${Day8.solutionP1(sampleResource1P1)} (expected: ${sampleResult1P1})")
    println("  Sample result 2: ${Day8.solutionP1(sampleResource2P1)} (expected: ${sampleResult2P1})")
    println("  Input result: ${Day8.solutionP1(inputResource)}")
    println("# Part 2")
    println("  Sample result: ${Day8.solutionP2(sampleResourceP2)} (expected: ${sampleResultP2})")
    println("  Input result: ${Day8.solutionP2(inputResource)}")
}

object Day8 {

    private val nodeParseRegex: Regex = Regex("([A-Z\\d]+)\\s+=\\s+\\(([A-Z\\d]+),\\s+([A-Z\\d]+)\\)")

    fun solutionP1(resource: Resource): Long = solution(
        resource = resource,
        initialNodePredicate = { it == "AAA" },
        finalNodePredicate = { it == "ZZZ" },
    )

    fun solutionP2(resource: Resource): Long = solution(
        resource = resource,
        initialNodePredicate = { it.endsWith("A") },
        finalNodePredicate = { it.endsWith("Z") },
    )

    private fun solution(
        resource: Resource,
        initialNodePredicate: (String) -> Boolean,
        finalNodePredicate: (String) -> Boolean,
    ): Long =
        resource.useLines { lines ->
            val (instructions, nodesByName) = parseState(lines)
            nodesByName.filterKeys(initialNodePredicate)
                .asSequence()
                .flatMap { (nodeName, _) ->
                    generateNodeSequence(nodeName, instructions, nodesByName)
                        .dropWhile { (curr, _) -> !finalNodePredicate(curr) }
                        .take(1)
                }
                .map { it.second }
                .leastCommonMultiple()
        }

    private fun generateNodeSequence(
        startingNodeName: String,
        instructions: String,
        nodesByName: Map<String, Node>
    ): Sequence<Pair<String, Long>> =
        generateSequence(startingNodeName to 0L) { (curr, step) ->
            val node = nodesByName[curr] ?: error("No node with name '$curr'")
            when (val direction = instructions[step.mod(instructions.length)]) {
                'L' -> node.leftNodeName to step + 1
                'R' -> node.rightNodeName to step + 1
                else -> error("Unknown direction '$direction'")
            }
        }

    private fun parseState(lines: Sequence<String>): ParseState =
        lines.fold(ParseState()) { state, line ->
            when {
                line.isBlank() -> state
                state.instructions.isBlank() -> state.copy(instructions = line)
                else -> {
                    val (_, curr, left, right) = nodeParseRegex.matchEntire(line)?.groupValues
                        ?: error("Failed to parse line: '$line'")
                    state.copy(nodesByName = state.nodesByName + (curr to Node(curr, left, right)))
                }
            }
        }

    private fun Sequence<Long>.leastCommonMultiple(): Long = reduce { acc, l -> leastCommonMultiple(acc, l) }

    private fun leastCommonMultiple(a: Long, b: Long): Long {
        val larger = if (a > b) a else b
        val maxLcm = a * b
        var lcm = larger
        while (lcm <= maxLcm) {
            if (lcm % a == 0L && lcm % b == 0L) {
                return lcm
            }
            lcm += larger
        }
        return maxLcm
    }

    private data class Node(
        val name: String,
        val leftNodeName: String,
        val rightNodeName: String,
    )

    private data class ParseState(
        val instructions: String = "",
        val nodesByName: Map<String, Node> = emptyMap()
    )
}
