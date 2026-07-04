package com.spot.android.data.feed

import kotlinx.serialization.Serializable

@Serializable
internal data class SpotLikeInsertDto(
    val user_id: String,
    val spot_id: String,
)

@Serializable
internal data class SpotBookmarkInsertDto(
    val user_id: String,
    val spot_id: String,
)
