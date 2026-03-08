package com.example.library_management.service

import com.example.library_management.controller.dto.BookRequest
import com.example.library_management.domain.Author
import com.example.library_management.domain.PublicationStatus
import com.example.library_management.repository.AuthorRepository
import com.example.library_management.repository.BookRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@Transactional
class BookServiceTest {

    @Autowired lateinit var bookService: BookService
    @Autowired lateinit var bookRepository: BookRepository
    @Autowired lateinit var authorRepository: AuthorRepository

    private fun createAuthor(name: String = "テスト著者"): Long {
        val author = Author(id = null, name = name, birthDate = LocalDate.of(1980, 1, 1))
        return authorRepository.insert(author).id!!
    }

    // 登録機能：正常系（未出版で登録）
    @Test
    fun `create - 未出版で書籍が登録されること`() {
        val authorId = createAuthor()
        val request = BookRequest(title = "吾輩は猫である", price = 1500, isPublished = null, authorIds = listOf(authorId))

        val response = bookService.create(request)

        // レスポンス検証
        assertNotNull(response.id)
        assertEquals("吾輩は猫である", response.title)
        assertEquals(1500, response.price)
        assertFalse(response.isPublished)
        assertEquals(1, response.authors.size)
        assertEquals(authorId, response.authors[0].id)

        // DB検証
        val saved = bookRepository.findById(response.id)
        assertNotNull(saved)
        assertEquals(PublicationStatus.UNPUBLISHED, saved!!.publicationStatus)
        assertEquals(listOf(authorId), saved.authorIds)
    }

    // 登録機能：正常系（出版済みで登録）
    @Test
    fun `create - 出版済みで書籍が登録されること`() {
        val authorId = createAuthor()
        val request = BookRequest(title = "坊っちゃん", price = 1200, isPublished = true, authorIds = listOf(authorId))

        val response = bookService.create(request)

        // レスポンス検証
        assertTrue(response.isPublished)

        // DB検証
        val saved = bookRepository.findById(response.id)
        assertNotNull(saved)
        assertEquals(PublicationStatus.PUBLISHED, saved!!.publicationStatus)
    }

    // 登録機能：正常系（複数著者で登録）
    @Test
    fun `create - 複数著者で書籍が登録されること`() {
        val authorId1 = createAuthor("著者1")
        val authorId2 = createAuthor("著者2")
        val request = BookRequest(title = "共著書籍", price = 2000, isPublished = null, authorIds = listOf(authorId1, authorId2))

        val response = bookService.create(request)

        // レスポンス検証
        assertEquals(2, response.authors.size)

        // DB検証
        val saved = bookRepository.findById(response.id)
        assertNotNull(saved)
        assertEquals(2, saved!!.authorIds.size)
        assertTrue(saved.authorIds.containsAll(listOf(authorId1, authorId2)))
    }

    // 更新機能：正常系（タイトル・価格・著者を変更、著者数は2から1に減らす）
    @Test
    fun `update - タイトル・価格・著者が更新され著者数が2から1に減ること`() {
        val authorId1 = createAuthor("著者1")
        val authorId2 = createAuthor("著者2")
        val authorId3 = createAuthor("著者3")
        val created = bookService.create(
            BookRequest(title = "旧タイトル", price = 1000, isPublished = null, authorIds = listOf(authorId1, authorId2)),
        )

        val updateRequest = BookRequest(title = "新タイトル", price = 2500, isPublished = null, authorIds = listOf(authorId3))
        val response = bookService.update(created.id, updateRequest)

        // レスポンス検証
        assertEquals("新タイトル", response.title)
        assertEquals(2500, response.price)
        assertEquals(1, response.authors.size)
        assertEquals(authorId3, response.authors[0].id)

        // DB検証
        val saved = bookRepository.findById(created.id)
        assertNotNull(saved)
        assertEquals("新タイトル", saved!!.title)
        assertEquals(2500, saved.price)
        assertEquals(listOf(authorId3), saved.authorIds)
    }
}
