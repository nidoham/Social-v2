package com.nidoham.social.helpers

import com.nidoham.social.domain.profile.Profile
import com.nidoham.social.domain.util.Paging
import com.nidoham.social.repository.UserRepository
import kotlinx.coroutines.runBlocking

object ProfileFetch {

    private lateinit var repository: UserRepository

    fun init(repo: UserRepository) {
        repository = repo
    }

    fun byId(id: String): Profile? = runBlocking {
        repository.getProfile(id).getOrNull()
    }

    fun byUsername(username: String): Profile? = runBlocking {
        repository.getProfileByUsername(username).getOrNull()
    }

    fun byEmail(email: String): Profile? = runBlocking {
        repository.getProfileByEmail(email).getOrNull()
    }

    fun byIds(ids: List<String>, page: Int = 1, size: Int = 20): Paging<Profile> = runBlocking {
        if (ids.isEmpty()) return@runBlocking Paging(emptyList(), page, size, 0, false)

        val startIndex = (page - 1) * size
        val endIndex = minOf(startIndex + size, ids.size)
        val pageIds = ids.subList(startIndex, endIndex)

        val profiles = repository.getProfiles(pageIds).getOrNull() ?: emptyList()

        Paging(
            data = profiles,
            page = page,
            size = size,
            total = ids.size,
            hasNext = endIndex < ids.size
        )
    }

    fun batch(ids: List<String>): List<Profile> = runBlocking {
        repository.getProfiles(ids).getOrNull() ?: emptyList()
    }

    fun followers(userId: String, page: Int = 1, size: Int = 20): Paging<Profile> = runBlocking {
        repository.getFollowers(userId, page, size).getOrNull()
            ?: Paging(emptyList(), page, size, 0, false)
    }

    fun following(userId: String, page: Int = 1, size: Int = 20): Paging<Profile> = runBlocking {
        repository.getFollowing(userId, page, size).getOrNull()
            ?: Paging(emptyList(), page, size, 0, false)
    }

    fun blocked(userId: String, page: Int = 1, size: Int = 20): Paging<Profile> = runBlocking {
        repository.getBlocked(userId, page, size).getOrNull()
            ?: Paging(emptyList(), page, size, 0, false)
    }

    fun mutual(userId: String, targetId: String, page: Int = 1, size: Int = 20): Paging<Profile> = runBlocking {
        repository.getMutualFollowers(userId, targetId, page, size).getOrNull()
            ?: Paging(emptyList(), page, size, 0, false)
    }

    fun exists(id: String): Boolean = byId(id) != null

    fun available(username: String): Boolean = byUsername(username) == null
}