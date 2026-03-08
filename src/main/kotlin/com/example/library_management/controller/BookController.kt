package com.example.library_management.controller

import com.example.library_management.controller.dto.BookRequest
import com.example.library_management.exception.NotFoundException
import com.example.library_management.service.AuthorService
import com.example.library_management.service.BookService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/books")
class BookController(
    private val bookService: BookService,
    private val authorService: AuthorService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: BookRequest): Map<String, Long> {
        request.authorIds!!.forEach { authorId ->
            authorService.findById(authorId) ?: throw NotFoundException("Author not found: id=$authorId")
        }
        val response = bookService.create(request)
        return mapOf("id" to response.id)
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun update(@PathVariable id: Long, @Valid @RequestBody request: BookRequest) {
        val existingBook = bookService.findById(id) ?: throw NotFoundException("Book not found: id=$id")
        if (existingBook.isPublished && request.isPublished == false) {
            throw IllegalArgumentException("Cannot change publication status from PUBLISHED to UNPUBLISHED")
        }
        request.authorIds!!.forEach { authorId ->
            authorService.findById(authorId) ?: throw NotFoundException("Author not found: id=$authorId")
        }
        bookService.update(id, request)
    }
}
