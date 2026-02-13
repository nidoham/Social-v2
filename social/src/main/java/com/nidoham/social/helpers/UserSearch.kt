package com.nidoham.social.helpers

import com.nidoham.social.domain.profile.Profile
import com.nidoham.social.domain.util.Order
import com.nidoham.social.domain.util.Paging
import com.nidoham.social.domain.util.SearchFilter
import com.nidoham.social.domain.util.SortBy
import com.nidoham.social.repository.UserRepository
import kotlinx.coroutines.runBlocking

object UserSearch {

    private lateinit var repository: UserRepository

    fun init(repo: UserRepository) {
        repository = repo
    }

    fun search(filter: SearchFilter, page: Int = 1, size: Int = 20): Paging<Profile> = runBlocking {
        repository.searchProfiles(filter, page, size).getOrNull()
            ?: Paging(emptyList(), page, size, 0, false)
    }

    fun byUsername(username: String, page: Int = 1, size: Int = 20): Paging<Profile> {
        val filter = SearchFilter(query = username, sortBy = SortBy.RELEVANT)
        return search(filter, page, size)
    }

    fun byName(name: String, page: Int = 1, size: Int = 20): Paging<Profile> {
        val filter = SearchFilter(query = name, sortBy = SortBy.NAME)
        return search(filter, page, size)
    }

    fun verified(page: Int = 1, size: Int = 20): Paging<Profile> {
        val filter = SearchFilter(verified = true, sortBy = SortBy.FOLLOWERS)
        return search(filter, page, size)
    }

    fun popular(minFollowers: Int = 1000, page: Int = 1, size: Int = 20): Paging<Profile> {
        val filter = SearchFilter(
            minFollowers = minFollowers,
            sortBy = SortBy.FOLLOWERS,
            order = Order.DESC
        )
        return search(filter, page, size)
    }

    fun recent(page: Int = 1, size: Int = 20): Paging<Profile> {
        val filter = SearchFilter(sortBy = SortBy.CREATED, order = Order.DESC)
        return search(filter, page, size)
    }

    fun advanced(
        query: String = "",
        verified: Boolean? = null,
        gender: String? = null,
        minFollowers: Int? = null,
        maxFollowers: Int? = null,
        sortBy: SortBy = SortBy.RELEVANT,
        page: Int = 1,
        size: Int = 20
    ): Paging<Profile> {
        val filter = SearchFilter(
            query = query,
            verified = verified,
            gender = gender,
            minFollowers = minFollowers,
            maxFollowers = maxFollowers,
            sortBy = sortBy
        )
        return search(filter, page, size)
    }
}