package com.nidoham.social.domain.user

import com.nidoham.social.domain.profile.Profile
import com.nidoham.social.domain.settings.Settings

data class User(
    val id: String,
    val profile: Profile,
    val settings: Settings,
    val following: List<String> = emptyList(),
    val followers: List<String> = emptyList(),
    val blocked: List<String> = emptyList()
)