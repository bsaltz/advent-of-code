package com.github.bsaltz.adventofcode.y2023.d06

import com.github.bsaltz.adventofcode.util.Resource
import com.github.bsaltz.adventofcode.util.StringUtils.tokenize
import com.github.bsaltz.adventofcode.util.toClassPathResource
import com.github.bsaltz.adventofcode.util.toStringResource
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

private const val sampleResultP1: Int = 288
private const val sampleResultP2: Int = 71503
private val sampleResource: Resource = """
    Time:      7  15   30
    Distance:  9  40  200
""".trimIndent().toStringResource()

private val inputResource: Resource = "2023/aoc-2023-d06-input.txt".toClassPathResource()

fun main() {
    println("# Part 1")
    println("  Sample result: ${Day6.solutionP1(sampleResource)} (expected: ${sampleResultP1})")
    println("  Input result: ${Day6.solutionP1(inputResource)}")
    println("# Part 2")
    println("  Sample result: ${Day6.solutionP2(sampleResource)} (expected: ${sampleResultP2})")
    println("  Input result: ${Day6.solutionP2(inputResource)}")
}

object Day6 {

    private const val ACCELERATION: Long = 1L // mm/ms^2

    fun solutionP1(resource: Resource): Long = solution(resource) { timeString, distanceString ->
        timeString.tokenize()
            .zip(distanceString.tokenize())
            .drop(1)
            .map { (time, distance) -> time.toLong() to distance.toLong() }
    }
    fun solutionP2(resource: Resource): Long = solution(resource) { timeString, distanceString ->
        val time = timeString.tokenize().drop(1).joinToString("").toLong()
        val distance = distanceString.tokenize().drop(1).joinToString("").toLong()
        listOf(time to distance)
    }

    private fun solution(resource: Resource, raceParser: (String, String) -> List<Pair<Long, Long>>): Long =
        resource.useLines { lines ->
            /*
             * This can be solved with some algebra. Consider the following equations:
             *   t_t = t_b + t_r;
             *     t_t is the total race time
             *     t_b is the button hold time
             *     t_r is the time after the button is released
             *   a = 1;
             *     a is the acceleration while the button is held in mm/ms²
             *   v = a * t_b;
             *     v is the velocity at t_b seconds in mm/ms
             *     v is 0 until t_b ms have passed
             *     v is constant after released
             *   d = v * t_r;
             *     d is the distance the car will travel in mm after t_r ms
             *
             * Since a, t_t, and d are given, we can use our equation for t_t to substitute and get d in terms of t_r
             * and our constants. We then turn that into a quadratic equation in standard form:
             *   d = a * t_t * t_r - a * t_r²
             *   0 = -a * t_r² + a * t_t * t_r - d
             *
             * Using the quadratic formula, we can find a formula for possible solutions of t_r:
             *   t_r = [ -a * t_t ± √(a² * t_t² - 4 * a * d_R) ] / (-2 * a)
             *
             * Using the solutions to t_r, we can define t_rmin and t_rmax to create a range gives us a way to calculate
             * the range in which our time-after-release must sit to travel further than the record. This range is
             * (t_rmin, t_rmax) and is continuous. The puzzle calls for discrete integer values, so we just need to find
             * the count of integer values within this range, excluding the min/max.
             */
            val (timeString, distanceString) = lines.toList()
            raceParser(timeString, distanceString)
                .asSequence()
                .map { (totalRaceTime, distanceRecord) ->
                    calculateTimeAfterRelease(totalRaceTime, distanceRecord)
                }
                .map { (first, second) ->
                    // This accounts for situations where e.g. min == 10.0. If min == 10.0, we want to return 11 because
                    // using the same t_r as the record will tie the record, not beat it.
                    val min = (minOf(first, second))
                    val max = (maxOf(first, second))
                    val minInt = (ceil(min).takeIf { it != min } ?: (min + 1)).toInt()
                    val maxInt = (floor(max).takeIf { it != max } ?: (max - 1)).toInt()
                    minInt to maxInt
                }
                .map { (min, max) -> (max - min + 1).toLong() }
                .reduce { acc, i -> acc * i }
        }

    private fun calculateTimeAfterRelease(totalRaceTime: Long, distance: Long): Pair<Double, Double> =
        quadratic(-ACCELERATION, ACCELERATION * totalRaceTime, -distance)

    // trunc( [ -b ± √(b² - 4ac) ] / [2a] )
    private fun quadratic(a: Long, b: Long, c: Long): Pair<Double, Double> =
        quadratic(a.toDouble(), b.toDouble(), c.toDouble())

    // [ -b ± √(b² - 4ac) ] / [2a]
    private fun quadratic(a: Double, b: Double, c: Double): Pair<Double, Double> =
        discriminant(a, b, c).pow(0.5).let { (-b + it) / (2 * a) to (-b - it) / (2 * a) }

    // b² - 4ac
    private fun discriminant(a: Double, b: Double, c: Double): Double =
        b.pow(2) - 4 * a * c
}
