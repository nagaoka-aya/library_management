package com.example.library_management.repository

import com.example.library_management.domain.Author
import com.example.library_management.exception.NotFoundException
import com.example.library_management.infrastructure.jooq.generated.Tables.AUTHOR
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class AuthorRepository(private val dsl: DSLContext) {

    fun insert(author: Author): Author {
        val record = dsl.insertInto(AUTHOR)
            .set(AUTHOR.NAME, author.name)
            .set(AUTHOR.BIRTH_DATE, author.birthDate)
            .returning(AUTHOR.ID, AUTHOR.NAME, AUTHOR.BIRTH_DATE)
            .fetchOne()!!
        return Author(
            id = record.get(AUTHOR.ID),
            name = record.get(AUTHOR.NAME),
            birthDate = record.get(AUTHOR.BIRTH_DATE),
        )
    }

    fun update(author: Author): Author {
        val updated = dsl.update(AUTHOR)
            .set(AUTHOR.NAME, author.name)
            .set(AUTHOR.BIRTH_DATE, author.birthDate)
            .where(AUTHOR.ID.eq(author.id))
            .execute()
        if (updated == 0) {
            throw NotFoundException("Author not found: id=${author.id}")
        }
        return author
    }

    fun findById(id: Long): Author? {
        val record = dsl.selectFrom(AUTHOR)
            .where(AUTHOR.ID.eq(id))
            .fetchOne() ?: return null
        return Author(
            id = record.get(AUTHOR.ID),
            name = record.get(AUTHOR.NAME),
            birthDate = record.get(AUTHOR.BIRTH_DATE),
        )
    }
}
