package com.example.library_management.controller.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class BookRequest(
    @field:NotBlank
    val title: String?,
    @field:NotNull
    @field:Min(0)
    val price: Int?,
    val isPublished: Boolean?,
    @field:NotNull
    @field:Size(min = 1)
    val authorIds: List<Long>?,
)
