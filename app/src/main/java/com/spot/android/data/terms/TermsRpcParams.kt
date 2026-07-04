package com.spot.android.data.terms

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RecordTermsAcceptanceRpcParams(
    @SerialName("p_app_version") val p_app_version: String? = null,
    @SerialName("p_build_number") val p_build_number: String? = null,
    @SerialName("p_device_info") val p_device_info: String? = null,
)
