package com.example.library_management.repository

import com.example.library_management.domain.Book
import com.example.library_management.domain.PublicationStatus
import com.example.library_management.infrastructure.jooq.generated.Tables.BOOK
import com.example.library_management.infrastructure.jooq.generated.Tables.BOOK_AUTHOR
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
class BookRepository(private val dsl: DSLContext) {

    fun insert(book: Book): Book {
        val record = dsl.insertInto(BOOK)
            .set(BOOK.TITLE, book.title)
            .set(BOOK.PRICE, BigDecimal.valueOf(book.price.toLong()))
            .set(BOOK.IS_PUBLISHED, book.publicationStatus == PublicationStatus.PUBLISHED)
            .returning(BOOK.ID)
            .fetchOne()!!
        val id = record.get(BOOK.ID)!!

        book.authorIds.forEach { authorId ->
            dsl.insertInto(BOOK_AUTHOR)
                .set(BOOK_AUTHOR.BOOK_ID, id)
                .set(BOOK_AUTHOR.AUTHOR_ID, authorId)
                .execute()
        }

        return book.copy(id = id)
    }

    fun update(book: Book): Book {
        dsl.update(BOOK)
            .set(BOOK.TITLE, book.title)
            .set(BOOK.PRICE, BigDecimal.valueOf(book.price.toLong()))
            .set(BOOK.IS_PUBLISHED, book.publicationStatus == PublicationStatus.PUBLISHED)
            .where(BOOK.ID.eq(book.id))
            .execute()

        dsl.deleteFrom(BOOK_AUTHOR)
            .where(BOOK_AUTHOR.BOOK_ID.eq(book.id))
            .execute()

        book.authorIds.forEach { authorId ->
            dsl.insertInto(BOOK_AUTHOR)
                .set(BOOK_AUTHOR.BOOK_ID, book.id)
                .set(BOOK_AUTHOR.AUTHOR_ID, authorId)
                .execute()
        }

        return book
    }

    fun findById(id: Long): Book? {
        val bookRecord = dsl.selectFrom(BOOK)
            .where(BOOK.ID.eq(id))
            .fetchOne() ?: return null

        val authorIds = dsl.select(BOOK_AUTHOR.AUTHOR_ID)
            .from(BOOK_AUTHOR)
            .where(BOOK_AUTHOR.BOOK_ID.eq(id))
            .fetch()
            .map { it.get(BOOK_AUTHOR.AUTHOR_ID)!! }

        return Book(
            id = bookRecord.get(BOOK.ID),
            title = bookRecord.get(BOOK.TITLE),
            price = bookRecord.get(BOOK.PRICE).toInt(),
            publicationStatus = if (bookRecord.get(BOOK.IS_PUBLISHED)) PublicationStatus.PUBLISHED else PublicationStatus.UNPUBLISHED,
            authorIds = authorIds,
        )
    }

    fun findByAuthorId(authorId: Long): List<Book> {
        val bookIds = dsl.select(BOOK_AUTHOR.BOOK_ID)
            .from(BOOK_AUTHOR)
            .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId))
            .orderBy(BOOK_AUTHOR.BOOK_ID.asc())
            .fetch()
            .map { it.get(BOOK_AUTHOR.BOOK_ID)!! }

        return bookIds.mapNotNull { findById(it) }
    }
}
