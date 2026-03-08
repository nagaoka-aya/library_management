package com.example.library_management.controller

import com.example.library_management.controller.dto.AuthorResponse
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

@WebMvcTest(AuthorController::class)
class AuthorControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var authorService: AuthorService

    @MockkBean
    lateinit var bookService: BookService

    // 登録機能：正常系（201 が返ること）
    @Test
    fun `POST authors - 正常系 201 が返ること`() {
        every { authorService.create(any()) } returns AuthorResponse(
            id = 1L,
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9),
        )

        mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"夏目漱石","birthDate":"1867-02-09"}"""),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
    }

    // 登録機能：name が null の場合に 400 が返ること
    @Test
    fun `POST authors - name が null の場合に 400 が返ること`() {
        mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"birthDate":"1867-02-09"}"""),
        )
            .andExpect(status().isBadRequest)
    }

    // 登録機能：name が空文字の場合に 400 が返ること
    @Test
    fun `POST authors - name が空文字の場合に 400 が返ること`() {
        mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"","birthDate":"1867-02-09"}"""),
        )
            .andExpect(status().isBadRequest)
    }

    // 登録機能：birthDate が null の場合に 400 が返ること
    @Test
    fun `POST authors - birthDate が null の場合に 400 が返ること`() {
        mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"夏目漱石"}"""),
        )
            .andExpect(status().isBadRequest)
    }

    // 登録機能：birthDate が未来日の場合に 400 が返ること
    @Test
    fun `POST authors - birthDate が未来日の場合に 400 が返ること`() {
        mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"未来の著者","birthDate":"2099-01-01"}"""),
        )
            .andExpect(status().isBadRequest)
    }

    // 更新機能：正常系（204 が返ること）
    @Test
    fun `PUT authors id - 正常系 204 が返ること`() {
        every { authorService.findById(1L) } returns AuthorResponse(
            id = 1L,
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9),
        )
        every { authorService.update(any(), any()) } returns AuthorResponse(
            id = 1L,
            name = "夏目金之助",
            birthDate = LocalDate.of(1867, 2, 9),
        )

        mockMvc.perform(
            put("/authors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"夏目金之助","birthDate":"1867-02-09"}"""),
        )
            .andExpect(status().isNoContent)
    }

    // 更新機能：name が空文字の場合に 400 が返ること
    @Test
    fun `PUT authors id - name が空文字の場合に 400 が返ること`() {
        mockMvc.perform(
            put("/authors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"","birthDate":"1867-02-09"}"""),
        )
            .andExpect(status().isBadRequest)
    }

    // 更新機能：birthDate が未来日の場合に 400 が返ること
    @Test
    fun `PUT authors id - birthDate が未来日の場合に 400 が返ること`() {
        mockMvc.perform(
            put("/authors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"未来の著者","birthDate":"2099-01-01"}"""),
        )
            .andExpect(status().isBadRequest)
    }

    // 更新機能：存在しない ID の場合に 404 が返ること
    @Test
    fun `PUT authors id - 存在しない ID の場合に 404 が返ること`() {
        every { authorService.findById(9999L) } returns null

        mockMvc.perform(
            put("/authors/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"存在しない著者","birthDate":"1990-01-01"}"""),
        )
            .andExpect(status().isNotFound)
    }

    // 著者IDに紐づく書籍一覧取得：正常系（200 と1件の書籍 JSON が返ること）
    @Test
    fun `GET authors id books - 正常系 200 と1件の書籍 JSON が返ること`() {
        every { bookService.findByAuthorId(1L) } returns listOf(
            BookResponse(id = 1L, title = "吾輩は猫である", price = 1500, isPublished = true, authors = listOf(AuthorSummary(id = 1L, name = "夏目漱石"))),
        )

        mockMvc.perform(get("/authors/1/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].title").value("吾輩は猫である"))
            .andExpect(jsonPath("$[0].price").value(1500))
            .andExpect(jsonPath("$[0].published").value(true))
    }

    // 著者IDに紐づく書籍一覧取得：正常系（200 と3件の書籍一覧 JSON が返ること）
    @Test
    fun `GET authors id books - 正常系 200 と3件の書籍一覧 JSON が返ること`() {
        every { bookService.findByAuthorId(1L) } returns listOf(
            BookResponse(id = 1L, title = "書籍A", price = 1000, isPublished = true, authors = listOf(AuthorSummary(id = 1L, name = "著者A"))),
            BookResponse(id = 2L, title = "書籍B", price = 1500, isPublished = false, authors = listOf(AuthorSummary(id = 1L, name = "著者A"))),
            BookResponse(id = 3L, title = "書籍C", price = 2000, isPublished = true, authors = listOf(AuthorSummary(id = 1L, name = "著者A"))),
        )

        mockMvc.perform(get("/authors/1/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(3))
            // ID昇順で返ること
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[2].id").value(3))
    }

    // 著者IDに紐づく書籍一覧取得：正常系（書籍が0件の場合に 200 と空配列が返ること）
    @Test
    fun `GET authors id books - 書籍が0件の場合に 200 と空配列が返ること`() {
        every { bookService.findByAuthorId(1L) } returns emptyList()

        mockMvc.perform(get("/authors/1/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    // 著者IDに紐づく書籍一覧取得：異常系（存在しない著者IDを指定した場合に 404 が返ること）
    @Test
    fun `GET authors id books - 存在しない著者IDを指定した場合に 404 が返ること`() {
        every { bookService.findByAuthorId(9999L) } returns null

        mockMvc.perform(get("/authors/9999/books"))
            .andExpect(status().isNotFound)
    }
}
