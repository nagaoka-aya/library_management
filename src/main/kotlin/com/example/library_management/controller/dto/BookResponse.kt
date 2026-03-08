package com.example.library_management.controller.dto

data class BookResponse(
    val id: Long,
    val title: String,
    val price: Int,
    val published: Boolean,
    val authors: List<AuthorSummary>,
)

data class AuthorSummary(
    val id: Long,
    val name: String,
)
