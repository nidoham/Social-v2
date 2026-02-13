package com.nidoham.social.domain.util

data class Paging<T>(
    val data: List<T>,
    val page: Int,
    val size: Int,
    val total: Int,
    val hasNext: Boolean
) {
    val totalPages: Int
        get() = (total + size - 1) / size

    val hasPrev: Boolean
        get() = page > 1
}