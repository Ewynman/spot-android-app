package com.spot.android.data.feed

import com.spot.android.data.model.Spot

/**
 * Light on-device diversity reordering for the first home page.
 *
 * Prevents a single dominant vibe from filling the visible window when other
 * vibes exist on the page. Cosmetic only — server ranking remains authoritative.
 *
 * Reference: PRD/16-feed-ranking-algorithm.md
 */
object FeedDiversity {

    private const val MAX_CONSECUTIVE_SAME_VIBE = 3
    private const val WINDOW_SIZE = 12

    fun apply(spots: List<Spot>, topVibeNames: List<String>): List<Spot> {
        if (spots.size <= 1) return spots

        val result = spots.toMutableList()
        var consecutive = 0
        var lastVibe: String? = null

        for (index in 0 until minOf(WINDOW_SIZE, result.size)) {
            val currentVibe = result[index].vibeTag
            if (currentVibe != null && currentVibe == lastVibe) {
                consecutive++
            } else {
                consecutive = 1
                lastVibe = currentVibe
            }

            if (consecutive <= MAX_CONSECUTIVE_SAME_VIBE) continue

            val swapIndex = (index + 1 until result.size).firstOrNull { candidate ->
                val candidateVibe = result[candidate].vibeTag
                candidateVibe != null && candidateVibe != currentVibe
            } ?: continue

            val temp = result[index]
            result[index] = result[swapIndex]
            result[swapIndex] = temp
            consecutive = 1
            lastVibe = result[index].vibeTag
        }

        return if (topVibeNames.isNotEmpty()) result else result
    }
}
