package com.nidoham.social.helpers

import com.nidoham.social.domain.profile.Profile
import com.nidoham.social.domain.util.Paging
import com.nidoham.social.repository.UserRepository
import kotlinx.coroutines.runBlocking

object PeopleList {

    private lateinit var repository: UserRepository

    fun init(repo: UserRepository) {
        repository = repo
    }

    fun new(page: Int = 1, size: Int = 20): Paging<Profile> = runBlocking {
        repository.getNewUsers(page, size).getOrNull()
            ?: Paging(emptyList(), page, size, 0, false)
    }

    fun popular(page: Int = 1, size: Int = 20): Paging<Profile> = runBlocking {
        repository.getPopularUsers(page, size).getOrNull()
            ?: Paging(emptyList(), page, size, 0, false)
    }

    fun active(page: Int = 1, size: Int = 20): Paging<Profile> = runBlocking {
        repository.getActiveUsers(page, size).getOrNull()
            ?: Paging(emptyList(), page, size, 0, false)
    }

    fun verified(page: Int = 1, size: Int = 20): Paging<Profile> = runBlocking {
        repository.getVerifiedUsers(page, size).getOrNull()
            ?: Paging(emptyList(), page, size, 0, false)
    }

    fun online(page: Int = 1, size: Int = 20): Paging<Profile> = runBlocking {
        // This would need additional implementation in repository
        Paging(emptyList(), page, size, 0, false)
    }

    fun suggested(userId: String, page: Int = 1, size: Int = 20): Paging<Profile> = runBlocking {
        repository.getSuggestedUsers(userId, page, size).getOrNull()
            ?: Paging(emptyList(), page, size, 0, false)
    }

    fun trending(days: Int = 7, page: Int = 1, size: Int = 20): Paging<Profile> = runBlocking {
        // This would need additional implementation with analytics
        Paging(emptyList(), page, size, 0, false)
    }

    fun random(size: Int = 20): List<Profile> = runBlocking {
        // Random sampling would need special repository method
        emptyList()
    }

    fun mightKnow(userId: String, page: Int = 1, size: Int = 20): Paging<Profile> = runBlocking {
        repository.getSuggestedUsers(userId, page, size).getOrNull()
            ?: Paging(emptyList(), page, size, 0, false)
    }
}