package com.spot.android.data.model

/**
 * Follow button state for a profile header.
 *
 * Reference: PRD/10-profile-social.md
 */
enum class FollowRelationship {
    /** Viewer is the profile owner. */
    Self,

    /** Public account — viewer is not following. */
    NotFollowing,

    /** Public account — viewer is following. */
    Following,

    /** Private account — viewer can send a request. */
    CanRequest,

    /** Private account — viewer has a pending outgoing request. */
    Requested,

    /** Private account — viewer is following (accepted). */
    FollowingPrivate,
}
