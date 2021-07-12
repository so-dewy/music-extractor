package com.dewy.musicextractish.spotify

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.client.WebClient
import java.io.ByteArrayInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class SpotifyController(val webClient: WebClient) {
    @GetMapping("/login/oauth2/code/spotify")
    fun redirect(
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {

        DefaultRedirectStrategy().sendRedirect(request, response, "http://localhost:3000")
    }

    @CrossOrigin("http://localhost:3000", allowCredentials = "true")
    @GetMapping("/spotify/user/playlists/export")
    fun exportPlaylists(
        @RegisteredOAuth2AuthorizedClient("spotify") authorizedClient: OAuth2AuthorizedClient,
        @RequestParam(required = false) ids: List<String>?,
        @RequestParam(required = false) selectAll: Boolean?
    ): Any {
        if (selectAll == true) {
            TODO("Collect all playlists id's and get request info for all of them")
        }

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest()
                .body("playlistIds is null or empty but should be populated when selectAll is not set")
        }
        val playlists = mutableListOf<Any>()
        ids.forEach {
            val resourceUri = "https://api.spotify.com/v1/playlists/${it}"
            val playlist = webClient
                .get()
                .uri(resourceUri)
                .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .bodyToMono(Any::class.java)
                .block()
            if (playlist != null) {
                playlists.add(playlist)
            }
        }
        val byteArray = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsBytes(playlists)
        return ResponseEntity
            .ok()
            .contentLength(byteArray.size.toLong())
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=playlists.json")
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(InputStreamResource(ByteArrayInputStream(byteArray)))
    }

    @CrossOrigin("http://localhost:3000", allowCredentials = "true")
    @GetMapping("/spotify/user/playlists")
    fun getUserPlaylists(
        @RegisteredOAuth2AuthorizedClient("spotify") authorizedClient: OAuth2AuthorizedClient
    ): String? {
        val resourceUri = "https://api.spotify.com/v1/me/playlists"
        return webClient
            .get()
            .uri(resourceUri)
            .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }

    @CrossOrigin("http://localhost:3000", allowCredentials = "true")
    @GetMapping("/spotify/user")
    fun getUserInfo(
        @RegisteredOAuth2AuthorizedClient("spotify") authorizedClient: OAuth2AuthorizedClient
    ): String? {
        val resourceUri = "https://api.spotify.com/v1/me"
        return webClient
            .get()
            .uri(resourceUri)
            .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }
}