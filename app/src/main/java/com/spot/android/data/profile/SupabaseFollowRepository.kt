package com.spot.android.data.profile

import com.spot.android.core.logging.LogCategory
import com.spot.android.core.logging.SpotLogger
import com.spot.android.core.supabase.SessionBridge
import com.spot.android.core.supabase.SupabaseClientProvider
import com.spot.android.data.dto.FollowRequestRowDto
import com.spot.android.data.dto.UserBriefRowDto
import com.spot.android.data.mapper.UserMapper
import com.spot.android.data.model.FollowRelationship
import com.spot.android.data.model.FollowRequest
import com.spot.android.data.model.enums.FollowRequestStatus
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable

/**
 * Supabase-backed follow repository.
 */
@Singleton
class SupabaseFollowRepository @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val sessionBridge: SessionBridge,
    private val logger: SpotLogger,
) : FollowRepository {

    private val postgrest get() = supabaseProvider.client.postgrest

    override suspend fun getFollowRelationship(targetUserId: String): Result<FollowRelationship> {
        val viewerId = sessionBridge.currentUserId
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        if (viewerId == targetUserId) {
            return Result.success(FollowRelationship.Self)
        }

        return try {
            val profile = postgrest.from("users_public")
                .select(columns = Columns.list("id", "is_private")) {
                    filter { eq("id", targetUserId) }
                }
                .decodeSingle<UserPrivacyRowDto>()

            val isFollowing = postgrest.from("follows")
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("follower_id", viewerId)
                        eq("followee_id", targetUserId)
                    }
                }
                .decodeList<FollowEdgeIdRowDto>()
                .isNotEmpty()

            if (isFollowing) {
                return Result.success(
                    if (profile.is_private) FollowRelationship.FollowingPrivate
                    else FollowRelationship.Following,
                )
            }

            if (profile.is_private) {
                val pendingOutgoing = postgrest.from("follow_requests")
                    .select(columns = Columns.list("id")) {
                        filter {
                            eq("requester_id", viewerId)
                            eq("target_user_id", targetUserId)
                            eq("status", FollowRequestStatus.PENDING.value)
                        }
                    }
                    .decodeList<FollowRequestIdRowDto>()
                    .isNotEmpty()

                return Result.success(
                    if (pendingOutgoing) FollowRelationship.Requested
                    else FollowRelationship.CanRequest,
                )
            }

            Result.success(FollowRelationship.NotFollowing)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to resolve follow relationship", e)
            Result.failure(e)
        }
    }

    override suspend fun followPublicUser(targetUserId: String): Result<Unit> {
        val viewerId = sessionBridge.currentUserId
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        return try {
            postgrest.from("follows").insert(
                FollowInsertDto(
                    follower_id = viewerId,
                    followee_id = targetUserId,
                ),
            )
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to follow user", e)
            Result.failure(e)
        }
    }

    override suspend fun unfollowUser(targetUserId: String): Result<Unit> {
        val viewerId = sessionBridge.currentUserId
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        return try {
            postgrest.from("follows").delete {
                filter {
                    eq("follower_id", viewerId)
                    eq("followee_id", targetUserId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to unfollow user", e)
            Result.failure(e)
        }
    }

    override suspend fun requestFollow(targetUserId: String): Result<Unit> {
        val viewerId = sessionBridge.currentUserId
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        return try {
            postgrest.from("follow_requests").insert(
                FollowRequestInsertDto(
                    requester_id = viewerId,
                    target_user_id = targetUserId,
                    status = FollowRequestStatus.PENDING.value,
                ),
            )
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to request follow", e)
            Result.failure(e)
        }
    }

    override suspend fun cancelFollowRequest(targetUserId: String): Result<Unit> {
        val viewerId = sessionBridge.currentUserId
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        return try {
            postgrest.from("follow_requests").delete {
                filter {
                    eq("requester_id", viewerId)
                    eq("target_user_id", targetUserId)
                    eq("status", FollowRequestStatus.PENDING.value)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to cancel follow request", e)
            Result.failure(e)
        }
    }

    override suspend fun getPendingIncomingCount(): Result<Int> {
        val viewerId = sessionBridge.currentUserId
            ?: return Result.success(0)

        return try {
            val count = postgrest.from("follow_requests")
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("target_user_id", viewerId)
                        eq("status", FollowRequestStatus.PENDING.value)
                    }
                }
                .decodeList<FollowRequestIdRowDto>()
                .size
            Result.success(count)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to load pending follow request count", e)
            Result.failure(e)
        }
    }

    override suspend fun getPendingIncomingRequests(
        offset: Int,
        limit: Int,
    ): Result<List<FollowRequest>> {
        val viewerId = sessionBridge.currentUserId
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        return try {
            val rows = postgrest.from("follow_requests")
                .select {
                    filter {
                        eq("target_user_id", viewerId)
                        eq("status", FollowRequestStatus.PENDING.value)
                    }
                    order(column = "created_at", order = Order.DESCENDING)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }
                .decodeList<FollowRequestRowDto>()

            if (rows.isEmpty()) return Result.success(emptyList())

            val requesterIds = rows.map { it.requester_id }.distinct()
            val requesters = postgrest.from("users_public")
                .select {
                    filter { isIn("id", requesterIds) }
                }
                .decodeList<UserBriefRowDto>()
                .associateBy { it.id }

            val requests = rows.mapNotNull { row ->
                val requester = requesters[row.requester_id] ?: return@mapNotNull null
                FollowRequest(
                    id = row.id,
                    requester = UserMapper.fromUserBriefRow(requester),
                    createdAt = com.spot.android.data.model.User.parseTimestamp(row.created_at),
                )
            }
            Result.success(requests)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to load follow requests", e)
            Result.failure(e)
        }
    }

    override suspend fun acceptFollowRequest(requestId: String, requesterId: String): Result<Unit> {
        val viewerId = sessionBridge.currentUserId
            ?: return Result.failure(IllegalStateException("Not authenticated"))

        return try {
            postgrest.from("follows").insert(
                FollowInsertDto(
                    follower_id = requesterId,
                    followee_id = viewerId,
                ),
            )
            postgrest.from("follow_requests").delete {
                filter { eq("id", requestId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to accept follow request", e)
            Result.failure(e)
        }
    }

    override suspend fun denyFollowRequest(requestId: String): Result<Unit> {
        return try {
            postgrest.from("follow_requests").delete {
                filter { eq("id", requestId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(LogCategory.Network, TAG, "Failed to deny follow request", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "SupabaseFollowRepository"
    }
}

@Serializable
private data class FollowInsertDto(
    val follower_id: String,
    val followee_id: String,
)

@Serializable
private data class FollowRequestInsertDto(
    val requester_id: String,
    val target_user_id: String,
    val status: String,
)

@Serializable
private data class FollowEdgeIdRowDto(
    val id: String,
)

@Serializable
private data class FollowRequestIdRowDto(
    val id: String,
)

@Serializable
private data class UserPrivacyRowDto(
    val id: String,
    val is_private: Boolean,
)
