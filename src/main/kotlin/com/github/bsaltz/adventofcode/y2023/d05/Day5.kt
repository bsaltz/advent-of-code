package com.github.bsaltz.adventofcode.y2023.d05

import com.github.bsaltz.adventofcode.util.RangeUtils.intersectRange
import com.github.bsaltz.adventofcode.util.RangeUtils.shiftRange
import com.github.bsaltz.adventofcode.util.Resource
import com.github.bsaltz.adventofcode.util.SequenceUtils.zipToPairs
import com.github.bsaltz.adventofcode.util.toClassPathResource
import com.github.bsaltz.adventofcode.util.toStringResource

private const val sampleResultP1: Int = 35
private const val sampleResultP2: Int = 46
private val sampleResource: Resource = """
    seeds: 79 14 55 13

    seed-to-soil map:
    50 98 2
    52 50 48

    soil-to-fertilizer map:
    0 15 37
    37 52 2
    39 0 15

    fertilizer-to-water map:
    49 53 8
    0 11 42
    42 0 7
    57 7 4

    water-to-light map:
    88 18 7
    18 25 70

    light-to-temperature map:
    45 77 23
    81 45 19
    68 64 13

    temperature-to-humidity map:
    0 69 1
    1 0 69

    humidity-to-location map:
    60 56 37
    56 93 4
""".trimIndent().toStringResource()

private val inputResource: Resource = "2023/aoc-2023-d05-input.txt".toClassPathResource()

fun main() {
    println("# Part 1")
    println("  Sample result: ${Day5.solutionP1(sampleResource)} (expected: ${sampleResultP1})")
    println("  Input result: ${Day5.solutionP1(inputResource)}")
    println("# Part 2")
    println("  Sample result: ${Day5.solutionP2(sampleResource)} (expected: ${sampleResultP2})")
    println("  Input result: ${Day5.solutionP2(inputResource)}")
}

object Day5 {

    fun solutionP1(resource: Resource): Long = solution(resource, ::parseSeedsP1)
    fun solutionP2(resource: Resource): Long = solution(resource, ::parseSeedsP2)

    private fun solution(resource: Resource, parseSeeds: (String) -> Sequence<LongRange>) =
        resource.useLines { lines ->
            lines.fold(Pair(emptySequence<LongRange>(), emptyList<Mapper>())) { pair, line ->
                val (values, mappers) = pair
                when {
                    line.startsWith("seeds: ") -> parseSeeds(line.removePrefix("seeds: ")) to mappers
                    line.isBlank() && mappers.isNotEmpty() ->
                        values.flatMap { value ->
                            val ranges = mappers.mapNotNull { it.map(value) }.takeIf { it.isNotEmpty() }
                                ?: listOf(IdentityMapper.map(value))
                            ranges
                        } to emptyList()
                    line.endsWith(" map:") -> values to emptyList()
                    line.isBlank() -> values to mappers
                    else -> values to mappers + parseRangeMapper(line)
                }
            }.first.minOf { it.first }
        }

    private fun parseSeedsP1(seedsStr: String): Sequence<LongRange> = seedsStr.trim().splitToSequence(" ")
        .map { it.toLong() }
        .map { it..it }

    private fun parseSeedsP2(seedsStr: String): Sequence<LongRange> = seedsStr.trim().splitToSequence(" ")
        .map { it.toLong() }
        .zipToPairs()
        .map { it.first..<(it.first + it.second) }

    private fun parseRangeMapper(rangeStr: String): RangeMapper =
        rangeStr.trim().split(" ").let { (destinationStr, sourceStr, lengthStr) ->
            RangeMapper(destinationStr.toLong(), sourceStr.toLong(), lengthStr.toLong())
        }

    private interface Mapper {
        fun map(input: Long): Long?
        fun map(inputs: LongRange): LongRange?
    }

    private object IdentityMapper : Mapper {
        override fun map(input: Long): Long = input
        override fun map(inputs: LongRange): LongRange = inputs
    }

    private data class RangeMapper(val destination: Long, val source: Long, val length: Long) : Mapper {
        private val range = source..<source + length
        override fun map(input: Long): Long? = input.takeIf { it in range }?.let { it - source + destination }
        override fun map(inputs: LongRange): LongRange? =
            range.intersectRange(inputs)?.shiftRange(destination - source)
    }
}
