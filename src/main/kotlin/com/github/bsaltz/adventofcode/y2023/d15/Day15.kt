package com.github.bsaltz.adventofcode.y2023.d15

import com.github.bsaltz.adventofcode.util.*

private const val sampleResultP1: Long = 1320L
private const val sampleResultP2: Long = 145L
private val sampleResource: Resource = """
    rn=1,cm-,qp=3,cm=2,qp-,pc=4,ot=9,ab=5,pc-,pc=6,ot=7
""".trimIndent().toStringResource()

private val inputResource: Resource = "2023/aoc-2023-d15-input.txt".toClassPathResource()

fun main() {
    println("# Part 1")
    println("  Sample result 1: ${Day15.solutionP1(sampleResource)} (expected: ${sampleResultP1})")
    println("  Input result: ${Day15.solutionP1(inputResource)}")
    println("# Part 2")
    println("  Sample result 2: ${Day15.solutionP2(sampleResource)} (expected: ${sampleResultP2})")
    println("  Input result: ${Day15.solutionP2(inputResource)}")
}

object Day15 {
    private val instructionRegex: Regex = Regex("([^=-]+)([-=])([1-9]?)")

    fun solutionP1(resource: Resource): Long = resource.useLines { lines ->
        lines.flatMap { it.split(",") }.sumOf { hash(it) }
    }

    fun solutionP2(resource: Resource): Long {
        val hashmap: MutableMap<Long, ArrayDeque<Lens>> = mutableMapOf()
        resource.useLines { lines -> lines.flatMap { it.split(",") }.toList() }
            .map { parse(it) }
            .forEach { runInstruction(it, hashmap) }
        return hashmap.focusingPower()
    }

    private fun hash(s: String): Long = s.fold(0L) { h, c -> (h + c.code) * 17L % 256L }

    private fun parse(instructionString: String): Instruction =
        instructionRegex.matchEntire(instructionString)?.groupValues?.let { (_, label, operation, length) ->
            Instruction(
                label = label,
                length = length.toIntOrNull(),
                operation = when (operation) {
                    "=" -> Operation.ADD
                    "-" -> Operation.REMOVE
                    else -> error("Unknown operation: $operation")
                }
            )
        } ?: error("No match found for $instructionString")

    private fun runInstruction(instruction: Instruction, hashmap: MutableMap<Long, ArrayDeque<Lens>>) {
        val box = hashmap.getOrPut(hash(instruction.label)) { ArrayDeque() }
        when (instruction.operation) {
            Operation.ADD -> {
                val lens = instruction.lens()
                val index = box.indexOfFirst { it.label == instruction.label }
                if (index >= 0) {
                    box.removeAt(index)
                    box.add(index, lens)
                } else {
                    box.addLast(lens)
                }
            }
            Operation.REMOVE -> {
                box.firstOrNull { it.label == instruction.label }?.let { box.remove(it) }
            }
        }
    }

    private fun Map<Long, ArrayDeque<Lens>>.focusingPower(): Long =
        entries.sumOf { (box, lenses) -> lenses.focusingPower(box) }
    private fun ArrayDeque<Lens>.focusingPower(box: Long): Long =
        mapIndexed { index, lens -> lens.focusingPower(box, index) }.sum()
    private fun Lens.focusingPower(boxNumber: Long, lensIndex: Int) =
        (boxNumber + 1) * (lensIndex + 1) * length

    private enum class Operation { ADD, REMOVE }
    private data class Lens(val label: String, val length: Int)
    private data class Instruction(val label: String, val length: Int?, val operation: Operation) {
        fun lens(): Lens = Lens(label, length ?: error("Cannot create lens with length from $this"))
    }
}