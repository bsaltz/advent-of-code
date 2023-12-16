package com.github.bsaltz.adventofcode.util

object RangeUtils {

    fun LongRange.intersectRange(other: LongRange): LongRange? {
        if (step != 1L || other.step != 1L) {
            TODO("This operation currently only supports ranges with a step value of 1,")
        }
        val thisFirst = first
        val thisLast = last
        val otherLast = other.last
        val otherFirst = other.first
        return if (thisFirst <= otherLast && otherFirst <= thisLast) {
            maxOf(thisFirst, otherFirst)..minOf(thisLast, otherLast)
        } else {
            null
        }
    }
    fun LongRange.shiftRange(distance: Long): LongRange = LongRange(start + distance, endInclusive + distance)
}
