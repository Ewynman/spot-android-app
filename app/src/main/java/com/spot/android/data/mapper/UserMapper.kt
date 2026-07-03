package com.spot.android.data.mapper

import com.spot.android.data.dto.UserBriefRowDto
import com.spot.android.data.dto.UserRowDto
import com.spot.android.data.model.User

/**
 * Mappers to convert user DTOs to domain models.
 * 
 * Reference: PRD/04-backend-api.md, PRD/10-profile-social.md
 */
object UserMapper {
    
    /**
     * Map UserBriefRowDto (public view) to User domain model.
     */
    fun fromUserBriefRow(dto: UserBriefRowDto, isCurrentUser: Boolean = false): User {
        return User(
            id = dto.id,
            username = dto.username,
            profileImageURL = dto.profile_image_url,
            isPrivate = dto.is_private,
            isPro = dto.is_pro,
            proUntil = User.parseTimestamp(dto.pro_until),
            spotsCount = dto.spots_count,
            isCurrentUser = isCurrentUser,
            createdAt = User.parseTimestamp(dto.created_at)
        )
    }
    
    /**
     * Map UserRowDto (own profile) to User domain model.
     */
    fun fromUserRow(dto: UserRowDto): User {
        return User(
            id = dto.id,
            username = dto.username,
            profileImageURL = dto.profile_image_url,
            isPrivate = dto.is_private,
            isPro = dto.is_pro,
            proUntil = User.parseTimestamp(dto.pro_until),
            spotsCount = dto.spots_count,
            isCurrentUser = true,
            createdAt = User.parseTimestamp(dto.created_at),
            email = dto.email,
            emailVerified = dto.email_verified
        )
    }
}
