package com.github.bsaltz.adventofcode.y2023.d12

import com.github.bsaltz.adventofcode.util.*
import com.github.bsaltz.adventofcode.util.LangUtils.tokenize

private const val sampleResultP1: Int = 21
private const val sampleResultP2: Int = 525152
private val sampleResource: Resource = """
    ???.### 1,1,3
    .??..??...?##. 1,1,3
    ?#?#?#?#?#?#?#? 1,3,1,6
    ????.#...#... 4,1,1
    ????.######..#####. 1,6,5
    ?###???????? 3,2,1
""".trimIndent().toStringResource()

private val inputResource: Resource = "2023/aoc-2023-d12-input.txt".toClassPathResource()

fun main() {
    println("# Part 1")
    println("  Sample result 1: ${Day12.solutionP1(sampleResource)} (expected: ${sampleResultP1})")
    println("  Input result: ${Day12.solutionP1(inputResource)}")
    println("# Part 2")
    println("  Sample result 2: ${Day12.solutionP2(sampleResource)} (expected: ${sampleResultP2})")
    println("  Input result: ${Day12.solutionP2(inputResource)}")
}

object Day12 {
    private val groupSplitRegex = Regex(",")

    fun solutionP1(resource: Resource): Long = resource.useLines { lines -> lines.sumOf { countSolutions(it) } }
    fun solutionP2(resource: Resource): Long = resource.useLines { lines -> lines.sumOf { countSolutions(it, 5) } }

    private fun countSolutions(line: String, folds: Int = 1): Long = parseLine(line).fold(folds)
        .let { (input: String, groups: List<Int>) -> countArrangements(input, groups) }

    private fun Pair<String, List<Int>>.fold(folds: Int): Pair<String, List<Int>> =
        let { (input, groups) -> List(folds) { input }.joinToString("?") to List(folds) { groups }.flatten() }

    private fun parseLine(line: String): Pair<String, List<Int>> =
        line.tokenize().let { (input, groupsString) -> input to parseGroups(groupsString) }

    private fun parseGroups(groupsString: String): List<Int> = groupsString.tokenize(groupSplitRegex) { it.toInt() }

    private val countArrangements: (String, List<Int>) -> Long =
        { input: String, groups: List<Int> -> doCountArrangements(input, groups) }.memoize()

    private fun doCountArrangements(input: String, groups: List<Int>): Long =
        input.firstOrNull().let { c ->
            when {
                groups.isEmpty() && '#' in input -> 0L
                groups.isEmpty() -> 1L
                c == null -> 0L
                c == '.' -> countArrangements(input.drop(1), groups)
                c == '#' -> {
                    if (continueRecursion(input, groups)) {
                        countArrangements(input.drop(groups.first() + 1), groups.drop(1))
                    } else {
                        0L
                    }
                }
                c == '?' -> countArrangements(".${input.drop(1)}", groups) + countArrangements("#${input.drop(1)}", groups)
                else -> error("Unknown condition: $input, $groups")
            }
        }

    /**
     * All of these conditions must be true to bother recursing:
     *
     * * `groups.first() <= input.length` — the current group we're processing must fit in the remaining input
     * * `"." !in input.take(groups.first())` — the next input characters required for the current group contain no
     *   `'.'`, only `'#'` and `'?'`
     * * `groups.first() == input.length || input[groups.first()] != '#')` — the current group is exactly the size of
     *   our current input _OR_ the character after the end of the current group isn't a `'#'`
     */
    private fun continueRecursion(input: String, groups: List<Int>): Boolean =
        groups.first() <= input.length &&
            "." !in input.take(groups.first()) &&
            (groups.first() == input.length || input[groups.first()] != '#')
}
