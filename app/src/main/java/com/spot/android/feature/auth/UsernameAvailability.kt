package com.spot.android.feature.auth

/**
 * Username availability state for sign-up.
 */
enum class UsernameAvailability {
    Unknown,
    Checking,
    Available,
    Taken,
}
