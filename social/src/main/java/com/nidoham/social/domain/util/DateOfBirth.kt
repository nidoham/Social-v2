package com.nidoham.social.domain.util

data class DateOfBirth(
    val day: Int,
    val month: Int,
    val year: Int
) {
    init {
        require(day in 1..31) { "Day must be between 1 and 31" }
        require(month in 1..12) { "Month must be between 1 and 12" }
        require(year > 1900) { "Year must be after 1900" }
    }
}