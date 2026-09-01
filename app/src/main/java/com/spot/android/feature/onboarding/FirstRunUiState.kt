package com.spot.android.feature.onboarding

/**
 * First-run coach tour steps. Order matches iOS SpotFirstRunOnboardingManager.Step.
 */
enum class FirstRunStep(val title: String, val body: String) {
    WELCOME(
        title = "Welcome to Spot",
        body = "Discover saved place recommendations from real people, organized by vibe.",
    ),
    SPOT_CARD(
        title = "Place first",
        body = "Every Spot leads with its location and vibe so you know what it is before you scroll.",
    ),
    SPOT_DETAILS(
        title = "Flip to the map",
        body = "Tap the green marker under the photo to peek at where a Spot is, or jump straight into the map.",
    ),
    VIBE_TAG(
        title = "Vibe Tags are the magic",
        body = "They describe how a place feels, not just what category it fits into.",
    ),
    LIKE(
        title = "Like what fits your taste",
        body = "Likes help you react to Spots that feel right.",
    ),
    BOOKMARK(
        title = "Save places for later",
        body = "Bookmark Spots you want to remember or visit.",
    ),
    CREATOR(
        title = "Follow people with great taste",
        body = "Spots come from people. Follow the ones who match your vibe.",
    ),
    MAP_TAB(
        title = "Now explore by location",
        body = "Tap Map to see Spots around you.",
    ),
    USER_LOCATION(
        title = "Start from where you are",
        body = "Your location helps you discover nearby recommendations.",
    ),
    MAP_MARKERS(
        title = "Markers show nearby Spots",
        body = "Tap a marker to preview the recommendation behind it.",
    ),
    MARKER_PREVIEW(
        title = "Open what catches your eye",
        body = "Move from a marker to the full Spot when a place looks interesting.",
    ),
    FINALE(
        title = "You are ready to explore",
        body = "Find places by vibe, save what you love, and follow people with great taste.",
    );

    val prefersFullScreenCard: Boolean
        get() = this == WELCOME || this == FINALE

    val canGoBack: Boolean
        get() = this != WELCOME && this != USER_LOCATION

    fun nextOrNull(): FirstRunStep? {
        val next = ordinal + 1
        return entries.getOrNull(next)
    }

    fun previousOrNull(): FirstRunStep? {
        val prev = ordinal - 1
        return entries.getOrNull(prev)
    }

    companion object {
        fun fromOrdinal(value: Int): FirstRunStep =
            entries.getOrElse(value) { WELCOME }
    }
}

data class FirstRunUiState(
    val isPresented: Boolean = false,
    val currentStep: FirstRunStep = FirstRunStep.WELCOME,
    val hasCompletedOrSkipped: Boolean = false,
) {
    val progress: Float
        get() {
            val denom = (FirstRunStep.entries.size - 1).coerceAtLeast(1).toFloat()
            return currentStep.ordinal / denom
        }

    val isFinale: Boolean
        get() = currentStep == FirstRunStep.FINALE
}
