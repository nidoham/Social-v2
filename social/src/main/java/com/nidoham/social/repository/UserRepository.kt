package com.nidoham.social.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.nidoham.social.domain.profile.Profile
import com.nidoham.social.domain.user.User
import com.nidoham.social.domain.util.Paging
import com.nidoham.social.domain.util.SearchFilter
import com.nidoham.social.domain.util.SortBy
import com.nidoham.social.domain.util.Order
import com.nidoham.social.helpers.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection("users")
    private val profilesCollection = firestore.collection("profiles")

    // ==================== USER CRUD ====================

    suspend fun createUser(user: User): Result<User> = runCatching {
        usersCollection.document(user.id).set(user).await()
        profilesCollection.document(user.id).set(user.profile).await()
        ProfileCache.put(user.id, user.profile)
        user
    }

    suspend fun getUser(userId: String): Result<User?> = runCatching {
        ProfileCache.get(userId)?.let { cachedProfile ->
            val userDoc = usersCollection.document(userId).get().await()
            userDoc.toObject<User>()?.copy(profile = cachedProfile)
        } ?: run {
            val userDoc = usersCollection.document(userId).get().await()
            val user = userDoc.toObject<User>()
            user?.profile?.let { ProfileCache.put(userId, it) }
            user
        }
    }

    suspend fun updateUser(user: User): Result<User> = runCatching {
        usersCollection.document(user.id).set(user).await()
        profilesCollection.document(user.id).set(user.profile).await()
        ProfileCache.put(user.id, user.profile)
        user
    }

    suspend fun deleteUser(userId: String): Result<Unit> = runCatching {
        usersCollection.document(userId).delete().await()
        profilesCollection.document(userId).delete().await()
        ProfileCache.remove(userId)
    }

    fun observeUser(userId: String): Flow<User?> = flow {
        usersCollection.document(userId).addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val user = snapshot?.toObject<User>()
            user?.profile?.let { ProfileCache.put(userId, it) }
        }
    }

    // ==================== PROFILE OPERATIONS ====================

    suspend fun getProfile(userId: String): Result<Profile?> = runCatching {
        ProfileCache.get(userId) ?: run {
            val doc = profilesCollection.document(userId).get().await()
            val profile = doc.toObject<Profile>()
            profile?.let { ProfileCache.put(userId, it) }
            profile
        }
    }

    suspend fun getProfileByUsername(username: String): Result<Profile?> = runCatching {
        val query = profilesCollection
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .await()

        val profile = query.documents.firstOrNull()?.toObject<Profile>()
        profile?.let { ProfileCache.put(it.username, it) }
        profile
    }

    suspend fun getProfileByEmail(email: String): Result<Profile?> = runCatching {
        val query = profilesCollection
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .await()

        val profile = query.documents.firstOrNull()?.toObject<Profile>()
        profile?.let { ProfileCache.put(profile.username, it) }
        profile
    }

    suspend fun getProfiles(userIds: List<String>): Result<List<Profile>> = runCatching {
        if (userIds.isEmpty()) return@runCatching emptyList()

        val cached = ProfileCache.getAll(userIds)
        val cachedIds = cached.map { it.username }
        val missingIds = userIds - cachedIds.toSet()

        if (missingIds.isEmpty()) return@runCatching cached

        val profiles = mutableListOf<Profile>()
        profiles.addAll(cached)

        missingIds.chunked(10).forEach { chunk ->
            val query = profilesCollection
                .whereIn("username", chunk)
                .get()
                .await()

            val fetchedProfiles = query.documents.mapNotNull { it.toObject<Profile>() }
            ProfileCache.putAll(fetchedProfiles)
            profiles.addAll(fetchedProfiles)
        }

        profiles
    }

    suspend fun updateProfile(userId: String, profile: Profile): Result<Profile> = runCatching {
        profilesCollection.document(userId).set(profile).await()

        val userDoc = usersCollection.document(userId).get().await()
        val user = userDoc.toObject<User>()
        user?.let {
            usersCollection.document(userId).set(it.copy(profile = profile)).await()
        }

        ProfileCache.put(userId, profile)
        profile
    }

    // ==================== RELATIONSHIPS ====================

    suspend fun followUser(currentUserId: String, targetUserId: String): Result<Unit> = runCatching {
        val currentUser = getUser(currentUserId).getOrThrow() ?: throw Exception("User not found")
        val targetUser = getUser(targetUserId).getOrThrow() ?: throw Exception("Target user not found")

        if (!UserRelationships.canFollow(currentUser, targetUserId)) {
            throw Exception("Cannot follow this user")
        }

        val updatedCurrentUser = UserOperations.addFollowing(currentUser, targetUserId)
        val updatedTargetUser = UserOperations.addFollower(targetUser, currentUserId)

        usersCollection.document(currentUserId).set(updatedCurrentUser).await()
        usersCollection.document(targetUserId).set(updatedTargetUser).await()

        profilesCollection.document(currentUserId).set(updatedCurrentUser.profile).await()
        profilesCollection.document(targetUserId).set(updatedTargetUser.profile).await()

        ProfileCache.put(currentUserId, updatedCurrentUser.profile)
        ProfileCache.put(targetUserId, updatedTargetUser.profile)
    }

    suspend fun unfollowUser(currentUserId: String, targetUserId: String): Result<Unit> = runCatching {
        val currentUser = getUser(currentUserId).getOrThrow() ?: throw Exception("User not found")
        val targetUser = getUser(targetUserId).getOrThrow() ?: throw Exception("Target user not found")

        if (!UserRelationships.canUnfollow(currentUser, targetUserId)) {
            throw Exception("Cannot unfollow this user")
        }

        val updatedCurrentUser = UserOperations.removeFollowing(currentUser, targetUserId)
        val updatedTargetUser = UserOperations.removeFollower(targetUser, currentUserId)

        usersCollection.document(currentUserId).set(updatedCurrentUser).await()
        usersCollection.document(targetUserId).set(updatedTargetUser).await()

        profilesCollection.document(currentUserId).set(updatedCurrentUser.profile).await()
        profilesCollection.document(targetUserId).set(updatedTargetUser.profile).await()

        ProfileCache.put(currentUserId, updatedCurrentUser.profile)
        ProfileCache.put(targetUserId, updatedTargetUser.profile)
    }

    suspend fun blockUser(currentUserId: String, targetUserId: String): Result<Unit> = runCatching {
        val currentUser = getUser(currentUserId).getOrThrow() ?: throw Exception("User not found")

        if (!UserRelationships.canBlock(currentUser, targetUserId)) {
            throw Exception("Cannot block this user")
        }

        val updatedUser = UserOperations.blockUser(currentUser, targetUserId)
        usersCollection.document(currentUserId).set(updatedUser).await()

        // Remove current user from target's followers if exists
        val targetUser = getUser(targetUserId).getOrThrow()
        targetUser?.let {
            if (UserRelationships.isFollower(it, currentUserId)) {
                val updatedTarget = UserOperations.removeFollower(it, currentUserId)
                usersCollection.document(targetUserId).set(updatedTarget).await()
                profilesCollection.document(targetUserId).set(updatedTarget.profile).await()
                ProfileCache.put(targetUserId, updatedTarget.profile)
            }
        }

        ProfileCache.put(currentUserId, updatedUser.profile)
    }

    suspend fun unblockUser(currentUserId: String, targetUserId: String): Result<Unit> = runCatching {
        val currentUser = getUser(currentUserId).getOrThrow() ?: throw Exception("User not found")

        if (!UserRelationships.canUnblock(currentUser, targetUserId)) {
            throw Exception("Cannot unblock this user")
        }

        val updatedUser = UserOperations.unblockUser(currentUser, targetUserId)
        usersCollection.document(currentUserId).set(updatedUser).await()
        ProfileCache.put(currentUserId, updatedUser.profile)
    }

    // ==================== FOLLOWERS & FOLLOWING ====================

    suspend fun getFollowers(userId: String, page: Int = 1, size: Int = 20): Result<Paging<Profile>> = runCatching {
        val user = getUser(userId).getOrThrow() ?: throw Exception("User not found")
        val followerIds = user.followers

        if (followerIds.isEmpty()) {
            return@runCatching Paging(emptyList(), page, size, 0, false)
        }

        val startIndex = (page - 1) * size
        val endIndex = minOf(startIndex + size, followerIds.size)
        val pageIds = followerIds.subList(startIndex, endIndex)

        val profiles = getProfiles(pageIds).getOrThrow()

        Paging(
            data = profiles,
            page = page,
            size = size,
            total = followerIds.size,
            hasNext = endIndex < followerIds.size
        )
    }

    suspend fun getFollowing(userId: String, page: Int = 1, size: Int = 20): Result<Paging<Profile>> = runCatching {
        val user = getUser(userId).getOrThrow() ?: throw Exception("User not found")
        val followingIds = user.following

        if (followingIds.isEmpty()) {
            return@runCatching Paging(emptyList(), page, size, 0, false)
        }

        val startIndex = (page - 1) * size
        val endIndex = minOf(startIndex + size, followingIds.size)
        val pageIds = followingIds.subList(startIndex, endIndex)

        val profiles = getProfiles(pageIds).getOrThrow()

        Paging(
            data = profiles,
            page = page,
            size = size,
            total = followingIds.size,
            hasNext = endIndex < followingIds.size
        )
    }

    suspend fun getBlocked(userId: String, page: Int = 1, size: Int = 20): Result<Paging<Profile>> = runCatching {
        val user = getUser(userId).getOrThrow() ?: throw Exception("User not found")
        val blockedIds = user.blocked

        if (blockedIds.isEmpty()) {
            return@runCatching Paging(emptyList(), page, size, 0, false)
        }

        val startIndex = (page - 1) * size
        val endIndex = minOf(startIndex + size, blockedIds.size)
        val pageIds = blockedIds.subList(startIndex, endIndex)

        val profiles = getProfiles(pageIds).getOrThrow()

        Paging(
            data = profiles,
            page = page,
            size = size,
            total = blockedIds.size,
            hasNext = endIndex < blockedIds.size
        )
    }

    suspend fun getMutualFollowers(userId: String, targetId: String, page: Int = 1, size: Int = 20): Result<Paging<Profile>> = runCatching {
        val user = getUser(userId).getOrThrow() ?: throw Exception("User not found")
        val targetUser = getUser(targetId).getOrThrow() ?: throw Exception("Target user not found")

        val mutualIds = UserRelationships.getMutualFollowers(user, targetUser)

        if (mutualIds.isEmpty()) {
            return@runCatching Paging(emptyList(), page, size, 0, false)
        }

        val startIndex = (page - 1) * size
        val endIndex = minOf(startIndex + size, mutualIds.size)
        val pageIds = mutualIds.subList(startIndex, endIndex)

        val profiles = getProfiles(pageIds).getOrThrow()

        Paging(
            data = profiles,
            page = page,
            size = size,
            total = mutualIds.size,
            hasNext = endIndex < mutualIds.size
        )
    }

    // ==================== SEARCH ====================

    suspend fun searchProfiles(filter: SearchFilter, page: Int = 1, size: Int = 20): Result<Paging<Profile>> = runCatching {
        var query: Query = profilesCollection

        // Apply filters
        if (filter.query.isNotBlank()) {
            query = query.orderBy("username")
                .startAt(filter.query)
                .endAt(filter.query + "\uf8ff")
        }

        filter.verified?.let {
            query = query.whereEqualTo("verified", it)
        }

        filter.banned?.let {
            query = query.whereEqualTo("banned", it)
        }

        filter.gender?.let {
            if (it.isNotBlank()) {
                query = query.whereEqualTo("gender", it)
            }
        }

        filter.minFollowers?.let {
            query = query.whereGreaterThanOrEqualTo("followers", it)
        }

        filter.maxFollowers?.let {
            query = query.whereLessThanOrEqualTo("followers", it)
        }

        // Apply sorting
        query = when (filter.sortBy) {
            SortBy.NAME -> query.orderBy("name", filter.order.toFirestore())
            SortBy.FOLLOWERS -> query.orderBy("followers", filter.order.toFirestore())
            SortBy.CREATED -> query.orderBy("created", filter.order.toFirestore())
            SortBy.UPDATED -> query.orderBy("updated", filter.order.toFirestore())
            SortBy.RELEVANT -> query // Already sorted by username if query exists
        }

        // Count total (expensive operation, consider caching)
        val totalSnapshot = query.get().await()
        val total = totalSnapshot.size()

        // Apply pagination
        val snapshot = query
            .limit(size.toLong())
            .get()
            .await()

        val profiles = snapshot.documents.mapNotNull { it.toObject<Profile>() }
        ProfileCache.putAll(profiles)

        Paging(
            data = profiles,
            page = page,
            size = size,
            total = total,
            hasNext = total > page * size
        )
    }

    // ==================== DISCOVERY ====================

    suspend fun getNewUsers(page: Int = 1, size: Int = 20): Result<Paging<Profile>> = runCatching {
        val query = profilesCollection
            .orderBy("created", Query.Direction.DESCENDING)
            .limit(size.toLong())
            .get()
            .await()

        val profiles = query.documents.mapNotNull { it.toObject<Profile>() }
        ProfileCache.putAll(profiles)

        val total = profilesCollection.get().await().size()

        Paging(
            data = profiles,
            page = page,
            size = size,
            total = total,
            hasNext = total > page * size
        )
    }

    suspend fun getPopularUsers(page: Int = 1, size: Int = 20): Result<Paging<Profile>> = runCatching {
        val query = profilesCollection
            .orderBy("followers", Query.Direction.DESCENDING)
            .limit(size.toLong())
            .get()
            .await()

        val profiles = query.documents.mapNotNull { it.toObject<Profile>() }
        ProfileCache.putAll(profiles)

        val total = profilesCollection.get().await().size()

        Paging(
            data = profiles,
            page = page,
            size = size,
            total = total,
            hasNext = total > page * size
        )
    }

    suspend fun getActiveUsers(page: Int = 1, size: Int = 20): Result<Paging<Profile>> = runCatching {
        val query = profilesCollection
            .orderBy("posts", Query.Direction.DESCENDING)
            .limit(size.toLong())
            .get()
            .await()

        val profiles = query.documents.mapNotNull { it.toObject<Profile>() }
        ProfileCache.putAll(profiles)

        val total = profilesCollection.get().await().size()

        Paging(
            data = profiles,
            page = page,
            size = size,
            total = total,
            hasNext = total > page * size
        )
    }

    suspend fun getVerifiedUsers(page: Int = 1, size: Int = 20): Result<Paging<Profile>> = runCatching {
        val query = profilesCollection
            .whereEqualTo("verified", true)
            .orderBy("followers", Query.Direction.DESCENDING)
            .limit(size.toLong())
            .get()
            .await()

        val profiles = query.documents.mapNotNull { it.toObject<Profile>() }
        ProfileCache.putAll(profiles)

        val totalQuery = profilesCollection.whereEqualTo("verified", true).get().await()
        val total = totalQuery.size()

        Paging(
            data = profiles,
            page = page,
            size = size,
            total = total,
            hasNext = total > page * size
        )
    }

    suspend fun getSuggestedUsers(userId: String, page: Int = 1, size: Int = 20): Result<Paging<Profile>> = runCatching {
        val user = getUser(userId).getOrThrow() ?: throw Exception("User not found")

        // Get friends of friends
        val followingProfiles = getProfiles(user.following).getOrThrow()
        val suggestedIds = mutableSetOf<String>()

        followingProfiles.forEach { profile ->
            val friendUser = getUser(profile.username).getOrNull()
            friendUser?.following?.forEach { friendOfFriendId ->
                if (friendOfFriendId != userId &&
                    friendOfFriendId !in user.following &&
                    friendOfFriendId !in user.blocked) {
                    suggestedIds.add(friendOfFriendId)
                }
            }
        }

        if (suggestedIds.isEmpty()) {
            return@runCatching getPopularUsers(page, size).getOrThrow()
        }

        val startIndex = (page - 1) * size
        val endIndex = minOf(startIndex + size, suggestedIds.size)
        val pageIds = suggestedIds.toList().subList(startIndex, endIndex)

        val profiles = getProfiles(pageIds).getOrThrow()

        Paging(
            data = profiles,
            page = page,
            size = size,
            total = suggestedIds.size,
            hasNext = endIndex < suggestedIds.size
        )
    }

    // ==================== VALIDATION ====================

    suspend fun isUsernameAvailable(username: String): Result<Boolean> = runCatching {
        getProfileByUsername(username).getOrNull() == null
    }

    suspend fun isEmailAvailable(email: String): Result<Boolean> = runCatching {
        getProfileByEmail(email).getOrNull() == null
    }

    // ==================== HELPERS ====================

    private fun Order.toFirestore(): Query.Direction {
        return when (this) {
            Order.ASC -> Query.Direction.ASCENDING
            Order.DESC -> Query.Direction.DESCENDING
        }
    }

    suspend fun incrementPostCount(userId: String): Result<Unit> = runCatching {
        val user = getUser(userId).getOrThrow() ?: throw Exception("User not found")
        val updatedProfile = UserOperations.incrementPosts(user.profile)
        val updatedUser = user.copy(profile = updatedProfile)

        updateUser(updatedUser).getOrThrow()
    }

    suspend fun decrementPostCount(userId: String): Result<Unit> = runCatching {
        val user = getUser(userId).getOrThrow() ?: throw Exception("User not found")
        val updatedProfile = UserOperations.decrementPosts(user.profile)
        val updatedUser = user.copy(profile = updatedProfile)

        updateUser(updatedUser).getOrThrow()
    }

    suspend fun updateOnlineStatus(userId: String): Result<Unit> = runCatching {
        val timestamp = com.google.firebase.Timestamp.now()
        profilesCollection.document(userId).update("online", timestamp).await()

        val profile = getProfile(userId).getOrThrow()
        profile?.let {
            val updated = it.copy(online = timestamp)
            ProfileCache.put(userId, updated)
        }
    }

    suspend fun updateSeenStatus(userId: String): Result<Unit> = runCatching {
        val timestamp = com.google.firebase.Timestamp.now()
        profilesCollection.document(userId).update("seen", timestamp).await()

        val profile = getProfile(userId).getOrThrow()
        profile?.let {
            val updated = it.copy(seen = timestamp)
            ProfileCache.put(userId, updated)
        }
    }
}