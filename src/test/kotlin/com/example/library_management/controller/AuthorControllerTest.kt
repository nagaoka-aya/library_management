package com.example.library_management.controller

import com.example.library_management.controller.dto.AuthorResponse
import com.example.library_management.exception.NotFoundException
import com.example.library_management.service.AuthorService
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
import java.time.LocalDate

@WebMvcTest(AuthorController::class)
class AuthorControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var authorService: AuthorService

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
}
