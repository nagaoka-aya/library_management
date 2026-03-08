package com.example.library_management.domain

enum class PublicationStatus {
    UNPUBLISHED,
    PUBLISHED,
}

data class Book(
    val id: Long?,
    val title: String,
    val price: Int?,
    val publicationStatus: PublicationStatus,
    val authorIds: List<Long>,
)
