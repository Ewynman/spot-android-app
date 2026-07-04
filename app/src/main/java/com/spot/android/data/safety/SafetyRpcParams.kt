package com.spot.android.data.safety

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SubmitContentReportRpcParams(
    @SerialName("p_target_type") val p_target_type: String,
    @SerialName("p_target_id") val p_target_id: String,
    @SerialName("p_reported_user_id") val p_reported_user_id: String,
    @SerialName("p_reason") val p_reason: String,
    @SerialName("p_details") val p_details: String = "",
    @SerialName("p_block_requested") val p_block_requested: Boolean = false,
)

@Serializable
internal data class BlockUserRpcParams(
    @SerialName("p_blocked_user_id") val p_blocked_user_id: String,
    @SerialName("p_source_target_type") val p_source_target_type: String? = null,
    @SerialName("p_source_target_id") val p_source_target_id: String? = null,
    @SerialName("p_reason") val p_reason: String? = null,
)
