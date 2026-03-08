package com.example.library_management.controller

import com.example.library_management.controller.dto.AuthorSummary
import com.example.library_management.controller.dto.BookResponse
import com.example.library_management.exception.NotFoundException
import com.example.library_management.service.AuthorService
import com.example.library_management.service.BookService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import com.example.library_management.controller.dto.AuthorResponse
import java.time.LocalDate

@WebMvcTest(BookController::class)
class BookControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var bookService: BookService

    @MockkBean
    lateinit var authorService: AuthorService

    private val authorResponse = AuthorResponse(id = 1L, name = "テスト著者", birthDate = LocalDate.of(1980, 1, 1))
    private val bookResponse = BookResponse(
        id = 1L,
        title = "テスト書籍",
        price = 1500,
        published = false,
        authors = listOf(AuthorSummary(id = 1L, name = "テスト著者")),
    )

    // 登録機能：正常系（201 が返ること）
    @Test
    fun `POST books - 正常系 201 が返ること`() {
        every { authorService.findById(1L) } returns authorResponse
        every { bookService.create(any()) } returns bookResponse

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"テスト書籍","price":1500,"published":false,"authorIds":[1]}"""),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
    }

    // 登録機能：異常系（title が空文字 → 400）
    @Test
    fun `POST books - title が空文字の場合に 400 が返ること`() {
        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"","price":1500,"authorIds":[1]}"""),
        )
            .andExpect(status().isBadRequest)
    }

    // 登録機能：異常系（price が null → 400）
    @Test
    fun `POST books - price が null の場合に 400 が返ること`() {
        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"テスト書籍","authorIds":[1]}"""),
        )
            .andExpect(status().isBadRequest)
    }

    // 登録機能：異常系（isPublished が null → 400）
    @Test
    fun `POST books - published が null の場合に 400 が返ること`() {
        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"テスト書籍","price":1500,"authorIds":[1]}"""),
        )
            .andExpect(status().isBadRequest)
    }

    // 登録機能：異常系（price が負の値 → 400）
    @Test
    fun `POST books - price が負の値の場合に 400 が返ること`() {
        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"テスト書籍","price":-1,"authorIds":[1]}"""),
        )
            .andExpect(status().isBadRequest)
    }

    // 登録機能：異常系（authorIds が null → 400）
    @Test
    fun `POST books - authorIds が null の場合に 400 が返ること`() {
        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"テスト書籍","price":1500}"""),
        )
            .andExpect(status().isBadRequest)
    }

    // 登録機能：異常系（authorIds が空リスト → 400）
    @Test
    fun `POST books - authorIds が空リストの場合に 400 が返ること`() {
        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"テスト書籍","price":1500,"authorIds":[]}"""),
        )
            .andExpect(status().isBadRequest)
    }

    // 登録機能：異常系（存在しない authorId と存在する authorId が混在 → 404）
    @Test
    fun `POST books - 存在しない authorId と存在する authorId が混在する場合に 404 が返ること`() {
        every { authorService.findById(1L) } returns authorResponse
        every { authorService.findById(999L) } returns null

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"テスト書籍","price":1500,"published":false,"authorIds":[1,999]}"""),
        )
            .andExpect(status().isNotFound)
    }

    // 更新機能：正常系（204 が返ること）
    @Test
    fun `PUT books id - 正常系 204 が返ること`() {
        every { bookService.findById(1L) } returns bookResponse
        every { authorService.findById(1L) } returns authorResponse
        every { bookService.update(any(), any()) } returns bookResponse

        mockMvc.perform(
            put("/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"新タイトル","price":2000,"published":false,"authorIds":[1]}"""),
        )
            .andExpect(status().isNoContent)
    }

    // 更新機能：正常系（未出版 → 出版済みに変更）
    @Test
    fun `PUT books id - 未出版から出版済みへの変更で 204 が返ること`() {
        val publishedBookResponse = bookResponse.copy(published = true)
        every { bookService.findById(1L) } returns bookResponse
        every { authorService.findById(1L) } returns authorResponse
        every { bookService.update(any(), any()) } returns publishedBookResponse

        mockMvc.perform(
            put("/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"テスト書籍","price":1500,"published":true,"authorIds":[1]}"""),
        )
            .andExpect(status().isNoContent)
    }

    // 更新機能：異常系（title が空文字 → 400）
    @Test
    fun `PUT books id - title が空文字の場合に 400 が返ること`() {
        mockMvc.perform(
            put("/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"","price":2000,"published":false,"authorIds":[1]}"""),
        )
            .andExpect(status().isBadRequest)
    }

    // 更新機能：異常系（price が負の値 → 400）
    @Test
    fun `PUT books id - price が負の値の場合に 400 が返ること`() {
        mockMvc.perform(
            put("/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"新タイトル","price":-1,"published":false,"authorIds":[1]}"""),
        )
            .andExpect(status().isBadRequest)
    }

    // 更新機能：異常系（isPublished が null → 400）
    @Test
    fun `PUT books id - published が null の場合に 400 が返ること`() {
        mockMvc.perform(
            put("/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"新タイトル","price":2000,"authorIds":[1]}"""),
        )
            .andExpect(status().isBadRequest)
    }

    // 更新機能：異常系（authorIds が空リスト → 400）
    @Test
    fun `PUT books id - authorIds が空リストの場合に 400 が返ること`() {
        mockMvc.perform(
            put("/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"新タイトル","price":2000,"published":false,"authorIds":[]}"""),
        )
            .andExpect(status().isBadRequest)
    }

    // 更新機能：異常系（存在しない書籍 ID → 404）
    @Test
    fun `PUT books id - 存在しない書籍 ID の場合に 404 が返ること`() {
        every { bookService.findById(9999L) } returns null

        mockMvc.perform(
            put("/books/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"新タイトル","price":2000,"published":false,"authorIds":[1]}"""),
        )
            .andExpect(status().isNotFound)
    }

    // 更新機能：異常系（存在しない authorId と存在する authorId が混在 → 404）
    @Test
    fun `PUT books id - 存在しない authorId と存在する authorId が混在する場合に 404 が返ること`() {
        every { bookService.findById(1L) } returns bookResponse
        every { authorService.findById(1L) } returns authorResponse
        every { authorService.findById(999L) } returns null

        mockMvc.perform(
            put("/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"新タイトル","price":2000,"published":false,"authorIds":[1,999]}"""),
        )
            .andExpect(status().isNotFound)
    }

    // 更新機能：異常系（出版済みから未出版への変更 → 400）
    @Test
    fun `PUT books id - 出版済みから未出版への変更で 400 が返ること`() {
        val publishedBookResponse = bookResponse.copy(published = true)
        every { bookService.findById(1L) } returns publishedBookResponse

        mockMvc.perform(
            put("/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"テスト書籍","price":1500,"published":false,"authorIds":[1]}"""),
        )
            .andExpect(status().isBadRequest)
    }
}
