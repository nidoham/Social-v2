package com.nidoham.social.repository

import com.nidoham.social.helpers.PeopleList
import com.nidoham.social.helpers.ProfileFetch
import com.nidoham.social.helpers.UserSearch

object RepositoryInitializer {

    private var initialized = false

    fun initialize(repository: UserRepository) {
        if (initialized) return

        ProfileFetch.init(repository)
        PeopleList.init(repository)
        UserSearch.init(repository)

        initialized = true
    }

    fun isInitialized(): Boolean = initialized
}