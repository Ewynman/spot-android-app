package com.spot.android.data.deeplink

/**
 * Parsed deep-link destinations. Mirror iOS DeepLinkRoute.
 */
sealed interface DeepLinkRoute {
    data class SpotDetail(val spotId: String) : DeepLinkRoute
    data object SubscriptionReturn : DeepLinkRoute
    data object Unknown : DeepLinkRoute
}

/**
 * Pure Kotlin URL parser (no Android Uri) so unit tests run on JVM.
 */
object DeepLinkRouter {
    private val spotIdRegex = Regex("^[a-zA-Z0-9_-]{1,50}$")

    fun isValidSpotId(spotId: String): Boolean = spotIdRegex.matches(spotId)

    fun parse(uriString: String): DeepLinkRoute {
        return try {
            val uri = java.net.URI(uriString)
            when (uri.scheme?.lowercase()) {
                "https", "http" -> parseHttp(uri)
                "spotapp" -> parseCustomScheme(uri)
                else -> DeepLinkRoute.Unknown
            }
        } catch (_: Exception) {
            DeepLinkRoute.Unknown
        }
    }

    private fun parseHttp(uri: java.net.URI): DeepLinkRoute {
        val host = uri.host?.lowercase().orEmpty()
        val allowed = host in setOf("spotapp.online", "www.spotapp.online", "localhost")
        if (!allowed && uri.scheme != "http") return DeepLinkRoute.Unknown
        if (uri.scheme == "http" && host != "localhost") return DeepLinkRoute.Unknown

        val parts = uri.path.orEmpty().split("/").filter { it.isNotBlank() }
        if (parts.size == 2 && parts[0] == "s") {
            val id = parts[1]
            return if (isValidSpotId(id)) DeepLinkRoute.SpotDetail(id) else DeepLinkRoute.Unknown
        }
        return DeepLinkRoute.Unknown
    }

    private fun parseCustomScheme(uri: java.net.URI): DeepLinkRoute {
        val host = uri.host?.lowercase()
        val parts = uri.path.orEmpty().split("/").filter { it.isNotBlank() }
        val query = uri.query.orEmpty()

        // spotapp://subscription/return
        if (host == "subscription" && parts.firstOrNull() == "return") {
            return DeepLinkRoute.SubscriptionReturn
        }
        // spotapp://open?spotId=
        if (host == "open") {
            val id = queryParam(query, "spotId")
            if (!id.isNullOrBlank() && isValidSpotId(id)) return DeepLinkRoute.SpotDetail(id)
        }
        // spotapp://spot/{id}
        if (host == "spot" && parts.size == 1) {
            val id = parts[0]
            if (isValidSpotId(id)) return DeepLinkRoute.SpotDetail(id)
        }
        // spotapp:///spot/{id}
        if (host == null && parts.size == 2 && parts[0] == "spot") {
            val id = parts[1]
            if (isValidSpotId(id)) return DeepLinkRoute.SpotDetail(id)
        }
        // spotapp:///subscription/return
        if (host == null && parts.size == 2 && parts[0] == "subscription" && parts[1] == "return") {
            return DeepLinkRoute.SubscriptionReturn
        }
        return DeepLinkRoute.Unknown
    }

    private fun queryParam(query: String, key: String): String? {
        return query.split("&")
            .mapNotNull { part ->
                val idx = part.indexOf('=')
                if (idx <= 0) return@mapNotNull null
                val k = part.substring(0, idx)
                val v = part.substring(idx + 1)
                if (k == key) v else null
            }
            .firstOrNull()
    }
}
