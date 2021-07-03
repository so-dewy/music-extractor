package com.dewy.musicextractish

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.config.Customizer
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@SpringBootApplication
class MusicExtractishApplication

fun main(args: Array<String>) {
    runApplication<MusicExtractishApplication>(*args)
}
