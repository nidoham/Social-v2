package com.nidoham.social.domain.settings.privacy

data class Privacy(
    val locked: Boolean = false,
    val private: Boolean = false,
    val status: Boolean = true
)