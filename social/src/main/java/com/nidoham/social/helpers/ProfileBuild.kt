package com.nidoham.social.helpers

import com.google.firebase.Timestamp
import com.nidoham.social.domain.profile.Profile
import com.nidoham.social.domain.util.DateOfBirth

object ProfileBuild {

    fun build(id: String): Profile {
        val now = Timestamp.now()

        return Profile(
            name = "",
            username = id,
            email = "",
            phone = "",
            bio = "",
            avatar = "",
            cover = "",
            verified = false,
            banned = false,
            created = now,
            updated = now,
            online = null,
            seen = null,
            birthday = null,
            gender = "",
            followers = 0,
            following = 0,
            posts = 0
        )
    }

    fun create(
        name: String,
        username: String,
        email: String,
        phone: String = "",
        bio: String = "",
        avatar: String = "",
        cover: String = "",
        birthday: DateOfBirth? = null,
        gender: String = ""
    ): Profile {
        val now = Timestamp.now()

        return Profile(
            name = name,
            username = username,
            email = email,
            phone = phone,
            bio = bio,
            avatar = avatar,
            cover = cover,
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
    }

    fun copy(
        profile: Profile,
        name: String? = null,
        bio: String? = null,
        avatar: String? = null,
        cover: String? = null,
        phone: String? = null,
        gender: String? = null,
        birthday: DateOfBirth? = null
    ): Profile {
        return profile.copy(
            name = name ?: profile.name,
            bio = bio ?: profile.bio,
            avatar = avatar ?: profile.avatar,
            cover = cover ?: profile.cover,
            phone = phone ?: profile.phone,
            gender = gender ?: profile.gender,
            birthday = birthday ?: profile.birthday,
            updated = Timestamp.now()
        )
    }
}