package com.example.library_management.controller

import org.jooq.DSLContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/health")
class HealthController(private val dsl: DSLContext) {

    @GetMapping
    fun health(): Map<String, String> = mapOf("status" to "ok")

    @GetMapping("/db")
    fun dbHealth(): Map<String, String> {
        dsl.fetch("SELECT 1")
        return mapOf("db" to "ok")
    }
}
