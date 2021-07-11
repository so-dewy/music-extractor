package com.dewy.musicextractish

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.RestController


@SpringBootApplication
@RestController
class MusicExtractishApplication

fun main(args: Array<String>) {
    runApplication<MusicExtractishApplication>(*args)
}