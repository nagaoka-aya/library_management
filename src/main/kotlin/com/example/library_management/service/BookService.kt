package com.example.library_management.service

import com.example.library_management.controller.dto.AuthorSummary
import com.example.library_management.controller.dto.BookRequest
import com.example.library_management.controller.dto.BookResponse
import com.example.library_management.domain.Book
import com.example.library_management.domain.PublicationStatus
import com.example.library_management.repository.AuthorRepository
import com.example.library_management.repository.BookRepository
import org.springframework.stereotype.Service

@Service
class BookService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
) {

    fun create(request: BookRequest): BookResponse {
        val book = Book(
            id = null,
            title = request.title!!,
            price = request.price!!,
            publicationStatus = if (request.isPublished == true) PublicationStatus.PUBLISHED else PublicationStatus.UNPUBLISHED,
            authorIds = request.authorIds!!,
        )
        val saved = bookRepository.insert(book)
        return toResponse(saved)
    }

    fun findById(id: Long): BookResponse? = bookRepository.findById(id)?.let { toResponse(it) }

    fun update(id: Long, request: BookRequest): BookResponse {
        val existing = bookRepository.findById(id)!!
        if (existing.publicationStatus == PublicationStatus.PUBLISHED && request.isPublished == false) {
            throw IllegalArgumentException("Cannot change publication status from PUBLISHED to UNPUBLISHED")
        }

        val newStatus = if (request.isPublished == true) PublicationStatus.PUBLISHED else existing.publicationStatus

        val book = Book(
            id = id,
            title = request.title!!,
            price = request.price!!,
            publicationStatus = newStatus,
            authorIds = request.authorIds!!,
        )
        val updated = bookRepository.update(book)
        return toResponse(updated)
    }

    private fun toResponse(book: Book): BookResponse {
        val authors = book.authorIds.map { authorId ->
            val author = authorRepository.findById(authorId)!!
            AuthorSummary(id = author.id!!, name = author.name)
        }
        return BookResponse(
            id = book.id!!,
            title = book.title,
            price = book.price,
            isPublished = book.publicationStatus == PublicationStatus.PUBLISHED,
            authors = authors,
        )
    }
}
