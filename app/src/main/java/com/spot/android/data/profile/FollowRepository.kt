package com.spot.android.data.profile

import com.spot.android.data.model.FollowRelationship
import com.spot.android.data.model.FollowRequest

/**
 * Repository for follow edges and follow requests.
 *
 * Reference: PRD/04-backend-api.md, PRD/10-profile-social.md
 */
interface FollowRepository {
    suspend fun getFollowRelationship(targetUserId: String): Result<FollowRelationship>
    suspend fun followPublicUser(targetUserId: String): Result<Unit>
    suspend fun unfollowUser(targetUserId: String): Result<Unit>
    suspend fun requestFollow(targetUserId: String): Result<Unit>
    suspend fun cancelFollowRequest(targetUserId: String): Result<Unit>
    suspend fun getPendingIncomingCount(): Result<Int>
    suspend fun getPendingIncomingRequests(
        offset: Int,
        limit: Int,
    ): Result<List<FollowRequest>>
    suspend fun acceptFollowRequest(requestId: String, requesterId: String): Result<Unit>
    suspend fun denyFollowRequest(requestId: String): Result<Unit>
}
