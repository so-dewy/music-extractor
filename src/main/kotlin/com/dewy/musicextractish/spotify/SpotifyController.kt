package com.dewy.musicextractish.spotify

import com.dewy.musicextractish.file.ExportType
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.client.WebClient
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class SpotifyController(val spotifyService: SpotifyService) {
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
        @RequestParam exportType: ExportType,
        @RequestParam(required = false) ids: List<String>?,
        @RequestParam(required = false) selectAll: Boolean?
    ): ResponseEntity<InputStreamResource> {
        if (selectAll == true) {
            return spotifyService.exportAllPlaylists(authorizedClient, exportType)
        }

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().build()
        }

        return spotifyService.exportPlaylists(authorizedClient, exportType, ids)
    }

    @CrossOrigin("http://localhost:3000", allowCredentials = "true")
    @GetMapping("/spotify/user/playlists")
    fun getUserPlaylists(
        @RegisteredOAuth2AuthorizedClient("spotify") authorizedClient: OAuth2AuthorizedClient
    ): String? {
        return spotifyService.getUserPlaylists(authorizedClient)
    }

    @CrossOrigin("http://localhost:3000", allowCredentials = "true")
    @GetMapping("/spotify/user")
    fun getUserInfo(
        @RegisteredOAuth2AuthorizedClient("spotify") authorizedClient: OAuth2AuthorizedClient
    ): String? {
        return spotifyService.getUserInfo(authorizedClient)
    }
}