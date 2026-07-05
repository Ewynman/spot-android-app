package com.spot.android.data.profile

import com.spot.android.data.model.FollowRelationship
import com.spot.android.data.model.FollowRequest
import com.spot.android.data.model.Spot
import com.spot.android.data.model.User

class FakeProfileRepository : ProfileRepository {
    var ownProfile: User? = null
    var publicProfiles: Map<String, User> = emptyMap()
    var spotIdsByUser: Map<String, List<String>> = emptyMap()
    var likedSpotIds: List<String> = emptyList()
    var bookmarkedSpotIds: List<String> = emptyList()
    var hydratedSpots: List<Spot> = emptyList()
    var deleteSpotResult: Result<Unit> = Result.success(Unit)
    var deletedSpotIds: MutableList<String> = mutableListOf()

    override suspend fun getOwnProfile(): Result<User> {
        return ownProfile?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("No own profile"))
    }

    override suspend fun getPublicProfile(userId: String): Result<User> {
        return publicProfiles[userId]?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("Profile not found"))
    }

    override suspend fun getUserSpotIds(
        userId: String,
        offset: Int,
        limit: Int,
    ): Result<List<String>> {
        val ids = spotIdsByUser[userId].orEmpty()
        return Result.success(ids.drop(offset).take(limit))
    }

    override suspend fun getLikedSpotIds(
        userId: String,
        offset: Int,
        limit: Int,
    ): Result<List<String>> = Result.success(likedSpotIds.drop(offset).take(limit))

    override suspend fun getBookmarkedSpotIds(
        userId: String,
        offset: Int,
        limit: Int,
    ): Result<List<String>> = Result.success(bookmarkedSpotIds.drop(offset).take(limit))

    override suspend fun hydrateSpots(spotIds: List<String>): Result<List<Spot>> {
        val byId = hydratedSpots.associateBy { it.id }
        return Result.success(spotIds.mapNotNull { byId[it] })
    }

    override suspend fun deleteOwnSpot(spotId: String): Result<Unit> {
        deletedSpotIds.add(spotId)
        return deleteSpotResult
    }
}

class FakeFollowRepository : FollowRepository {
    var relationship: FollowRelationship = FollowRelationship.Self
    var pendingCount: Int = 0
    var pendingRequests: List<FollowRequest> = emptyList()
    var followCalls: Int = 0
    var unfollowCalls: Int = 0
    var requestCalls: Int = 0
    var cancelCalls: Int = 0

    override suspend fun getFollowRelationship(targetUserId: String): Result<FollowRelationship> {
        return Result.success(relationship)
    }

    override suspend fun followPublicUser(targetUserId: String): Result<Unit> {
        followCalls++
        relationship = FollowRelationship.Following
        return Result.success(Unit)
    }

    override suspend fun unfollowUser(targetUserId: String): Result<Unit> {
        unfollowCalls++
        relationship = FollowRelationship.NotFollowing
        return Result.success(Unit)
    }

    override suspend fun requestFollow(targetUserId: String): Result<Unit> {
        requestCalls++
        relationship = FollowRelationship.Requested
        return Result.success(Unit)
    }

    override suspend fun cancelFollowRequest(targetUserId: String): Result<Unit> {
        cancelCalls++
        relationship = FollowRelationship.CanRequest
        return Result.success(Unit)
    }

    override suspend fun getPendingIncomingCount(): Result<Int> = Result.success(pendingCount)

    override suspend fun getPendingIncomingRequests(
        offset: Int,
        limit: Int,
    ): Result<List<FollowRequest>> = Result.success(pendingRequests.drop(offset).take(limit))

    override suspend fun acceptFollowRequest(requestId: String, requesterId: String): Result<Unit> {
        pendingRequests = pendingRequests.filterNot { it.id == requestId }
        pendingCount = pendingRequests.size
        return Result.success(Unit)
    }

    override suspend fun denyFollowRequest(requestId: String): Result<Unit> {
        pendingRequests = pendingRequests.filterNot { it.id == requestId }
        pendingCount = pendingRequests.size
        return Result.success(Unit)
    }
}
