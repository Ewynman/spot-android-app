package com.spot.android.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SpotLikeRowDto(
    val spot_id: String,
)

@Serializable
data class SpotBookmarkRowDto(
    val spot_id: String,
)

@Serializable
data class UserBlockRowDto(
    val blocked_user_id: String,
)
