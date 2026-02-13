package com.nidoham.social.helpers

import com.nidoham.social.domain.user.User

object UserRelationships {

    fun isFollowing(user: User, userId: String): Boolean = userId in user.following

    fun isFollower(user: User, userId: String): Boolean = userId in user.followers

    fun isBlocked(user: User, userId: String): Boolean = userId in user.blocked

    fun isMutual(user: User, userId: String): Boolean =
        isFollowing(user, userId) && isFollower(user, userId)

    fun getMutualFollowers(user1: User, user2: User): List<String> {
        return user1.followers.intersect(user2.followers.toSet()).toList()
    }

    fun getFollowersNotFollowingBack(user: User): List<String> {
        return user.followers.filterNot { it in user.following }
    }

    fun getFollowingNotFollowingBack(user: User): List<String> {
        return user.following.filterNot { it in user.followers }
    }

    fun canFollow(user: User, targetUserId: String): Boolean {
        return !isFollowing(user, targetUserId) && !isBlocked(user, targetUserId)
    }

    fun canUnfollow(user: User, targetUserId: String): Boolean {
        return isFollowing(user, targetUserId)
    }

    fun canBlock(user: User, targetUserId: String): Boolean {
        return !isBlocked(user, targetUserId) && targetUserId != user.id
    }

    fun canUnblock(user: User, targetUserId: String): Boolean {
        return isBlocked(user, targetUserId)
    }

    enum class RelationshipStatus {
        NONE,
        FOLLOWING,
        FOLLOWER,
        MUTUAL,
        BLOCKED
    }

    fun getRelationshipStatus(currentUser: User, targetUserId: String): RelationshipStatus {
        if (isBlocked(currentUser, targetUserId)) {
            return RelationshipStatus.BLOCKED
        }

        val following = isFollowing(currentUser, targetUserId)
        val follower = isFollower(currentUser, targetUserId)

        return when {
            following && follower -> RelationshipStatus.MUTUAL
            following -> RelationshipStatus.FOLLOWING
            follower -> RelationshipStatus.FOLLOWER
            else -> RelationshipStatus.NONE
        }
    }

    data class RelationshipInfo(
        val isFollowing: Boolean,
        val isFollower: Boolean,
        val isMutual: Boolean,
        val isBlocked: Boolean,
        val status: RelationshipStatus
    )

    fun getRelationshipInfo(currentUser: User, targetUserId: String): RelationshipInfo {
        val following = isFollowing(currentUser, targetUserId)
        val follower = isFollower(currentUser, targetUserId)
        val mutual = following && follower
        val blocked = isBlocked(currentUser, targetUserId)
        val status = getRelationshipStatus(currentUser, targetUserId)

        return RelationshipInfo(following, follower, mutual, blocked, status)
    }
}