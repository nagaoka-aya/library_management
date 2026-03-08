package com.example.library_management.service

import com.example.library_management.controller.dto.AuthorRequest
import com.example.library_management.repository.AuthorRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@Transactional
class AuthorServiceTest {

    @Autowired lateinit var authorService: AuthorService
    @Autowired lateinit var authorRepository: AuthorRepository

    // 登録機能：正常系（著者が登録されリポジトリの戻り値が返ること）
    @Test
    fun `create - 著者が登録されDBに保存されること`() {
        val request = AuthorRequest(name = "夏目漱石", birthDate = LocalDate.of(1867, 2, 9))

        val response = authorService.create(request)

        // レスポンスの検証
        assertNotNull(response.id)
        assertEquals("夏目漱石", response.name)
        assertEquals(LocalDate.of(1867, 2, 9), response.birthDate)

        // DBに実際に保存されていることを検証
        val saved = authorRepository.findById(response.id)
        assertNotNull(saved)
        assertEquals("夏目漱石", saved!!.name)
        assertEquals(LocalDate.of(1867, 2, 9), saved.birthDate)
    }

    // 更新機能：正常系（著者情報が更新されDBに反映されること）
    @Test
    fun `update - 著者情報が更新されDBに反映されること`() {
        val created = authorService.create(
            AuthorRequest(name = "夏目漱石", birthDate = LocalDate.of(1867, 2, 9)),
        )

        authorService.update(created.id, AuthorRequest(name = "夏目金之助", birthDate = LocalDate.of(1867, 2, 9)))

        // DBの値が更新されていることを検証
        val updated = authorRepository.findById(created.id)
        assertNotNull(updated)
        assertEquals("夏目金之助", updated!!.name)
        assertEquals(LocalDate.of(1867, 2, 9), updated.birthDate)
    }

    // findById：存在しない ID の場合に null が返ること
    @Test
    fun `findById - 存在しない ID の場合に null が返ること`() {
        val result = authorService.findById(9999L)
        assertNull(result)
    }
}
