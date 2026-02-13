package com.nidoham.social.domain.profile

import com.google.firebase.Timestamp
import com.nidoham.social.domain.util.DateOfBirth

data class Profile(
    val name: String,
    val username: String,
    val email: String,
    val phone: String = "",
    val bio: String = "",
    val avatar: String = "",
    val cover: String = "",
    val verified: Boolean = false,
    val banned: Boolean = false,
    val created: Timestamp,
    val updated: Timestamp,
    val online: Timestamp? = null,
    val seen: Timestamp? = null,
    val birthday: DateOfBirth? = null,
    val gender: String = "",
    val followers: Int = 0,
    val following: Int = 0,
    val posts: Int = 0
)