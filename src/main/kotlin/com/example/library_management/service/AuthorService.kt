package com.example.library_management.service

import com.example.library_management.controller.dto.AuthorRequest
import com.example.library_management.controller.dto.AuthorResponse
import com.example.library_management.domain.Author
import com.example.library_management.repository.AuthorRepository
import org.springframework.stereotype.Service

@Service
class AuthorService(private val authorRepository: AuthorRepository) {

    fun create(request: AuthorRequest): AuthorResponse {
        val author = Author(id = null, name = request.name!!, birthDate = request.birthDate!!)
        val saved = authorRepository.insert(author)
        return toResponse(saved)
    }

    fun update(id: Long, request: AuthorRequest): AuthorResponse {
        val author = Author(id = id, name = request.name!!, birthDate = request.birthDate!!)
        val updated = authorRepository.update(author)
        return toResponse(updated)
    }

    private fun toResponse(author: Author) = AuthorResponse(
        id = author.id!!,
        name = author.name,
        birthDate = author.birthDate,
    )
}

