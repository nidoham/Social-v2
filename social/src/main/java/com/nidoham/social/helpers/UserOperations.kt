package com.nidoham.social.helpers

import com.nidoham.social.domain.profile.Profile
import com.nidoham.social.domain.user.User

object UserOperations {

    private fun incrementFollowers(profile: Profile): Profile {
        return profile.copy(followers = profile.followers + 1)
    }

    private fun decrementFollowers(profile: Profile): Profile {
        return profile.copy(followers = maxOf(0, profile.followers - 1))
    }

    private fun incrementFollowing(profile: Profile): Profile {
        return profile.copy(following = profile.following + 1)
    }

    private fun decrementFollowing(profile: Profile): Profile {
        return profile.copy(following = maxOf(0, profile.following - 1))
    }

    fun incrementPosts(profile: Profile): Profile {
        return profile.copy(posts = profile.posts + 1)
    }

    fun decrementPosts(profile: Profile): Profile {
        return profile.copy(posts = maxOf(0, profile.posts - 1))
    }

    fun addFollower(user: User, followerId: String): User {
        if (followerId in user.followers) return user
        return user.copy(
            followers = user.followers + followerId,
            profile = incrementFollowers(user.profile)
        )
    }

    fun removeFollower(user: User, followerId: String): User {
        if (followerId !in user.followers) return user
        return user.copy(
            followers = user.followers - followerId,
            profile = decrementFollowers(user.profile)
        )
    }

    fun addFollowing(user: User, followingId: String): User {
        if (followingId in user.following) return user
        return user.copy(
            following = user.following + followingId,
            profile = incrementFollowing(user.profile)
        )
    }

    fun removeFollowing(user: User, followingId: String): User {
        if (followingId !in user.following) return user
        return user.copy(
            following = user.following - followingId,
            profile = decrementFollowing(user.profile)
        )
    }

    fun blockUser(user: User, blockedUserId: String): User {
        if (blockedUserId in user.blocked) return user
        var updatedUser = user.copy(blocked = user.blocked + blockedUserId)

        if (blockedUserId in updatedUser.followers) {
            updatedUser = removeFollower(updatedUser, blockedUserId)
        }
        if (blockedUserId in updatedUser.following) {
            updatedUser = removeFollowing(updatedUser, blockedUserId)
        }

        return updatedUser
    }

    fun unblockUser(user: User, blockedUserId: String): User {
        if (blockedUserId !in user.blocked) return user
        return user.copy(blocked = user.blocked - blockedUserId)
    }

    fun follow(currentUser: User, targetUserId: String): User {
        return addFollowing(currentUser, targetUserId)
    }

    fun unfollow(currentUser: User, targetUserId: String): User {
        return removeFollowing(currentUser, targetUserId)
    }
}