package com.nidoham.social.helpers

import com.google.firebase.Timestamp
import com.nidoham.social.domain.profile.Profile
import com.nidoham.social.domain.settings.Settings
import com.nidoham.social.domain.settings.privacy.Privacy
import com.nidoham.social.domain.user.User
import com.nidoham.social.domain.util.DateOfBirth

object UserBuild {

    fun create(
        id: String,
        name: String,
        username: String,
        email: String,
        phone: String = "",
        bio: String = "",
        birthday: DateOfBirth? = null,
        gender: String = ""
    ): User {
        val now = Timestamp.now()

        val profile = Profile(
            name = name,
            username = username,
            email = email,
            phone = phone,
            bio = bio,
            avatar = "",
            cover = "",
            verified = false,
            banned = false,
            created = now,
            updated = now,
            online = now,
            seen = now,
            birthday = birthday,
            gender = gender,
            followers = 0,
            following = 0,
            posts = 0
        )

        return User(
            id = id,
            profile = profile,
            settings = Settings(privacy = Privacy()),
            following = emptyList(),
            followers = emptyList(),
            blocked = emptyList()
        )
    }

    fun updateProfile(
        user: User,
        name: String? = null,
        bio: String? = null,
        avatar: String? = null,
        cover: String? = null,
        phone: String? = null,
        gender: String? = null,
        birthday: DateOfBirth? = null
    ): User {
        return user.copy(
            profile = user.profile.copy(
                name = name ?: user.profile.name,
                bio = bio ?: user.profile.bio,
                avatar = avatar ?: user.profile.avatar,
                cover = cover ?: user.profile.cover,
                phone = phone ?: user.profile.phone,
                gender = gender ?: user.profile.gender,
                birthday = birthday ?: user.profile.birthday,
                updated = Timestamp.now()
            )
        )
    }
}