package com.nidoham.social.domain.settings

import com.nidoham.social.domain.settings.privacy.Privacy

data class Settings(
    val privacy: Privacy = Privacy()
)