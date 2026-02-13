package com.nidoham.social.domain.util

data class SearchFilter(
    val query: String = "",
    val verified: Boolean? = null,
    val banned: Boolean? = null,
    val gender: String? = null,
    val minFollowers: Int? = null,
    val maxFollowers: Int? = null,
    val sortBy: SortBy = SortBy.RELEVANT,
    val order: Order = Order.DESC
)

enum class SortBy {
    RELEVANT,
    NAME,
    FOLLOWERS,
    CREATED,
    UPDATED
}

enum class Order {
    ASC,
    DESC
}