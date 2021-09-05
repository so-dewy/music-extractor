package com.dewy.musicextractish.spotify

import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
        val fileName = prepareFileName(exportType)
        if (selectAll == true) {
            val playlistExportResult = spotifyService.exportAllPlaylists(authorizedClient, exportType)
            return prepareResponse(playlistExportResult, fileName)
        }

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().build()
        }

        val playlistExportResult = spotifyService.exportPlaylists(authorizedClient, exportType, ids)

        return prepareResponse(playlistExportResult, fileName)
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

    private fun prepareResponse(playlistExportResult: PlaylistExportResult, fileName: String): ResponseEntity<InputStreamResource> {
        return ResponseEntity
            .ok()
            .contentLength(playlistExportResult.contentLength)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$fileName")
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(playlistExportResult.inputStreamResource)
    }

    private fun prepareFileName(exportType: ExportType): String {
        val date = LocalDateTime.now()
        val fileNamePostfix = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))

        return "playlists_$fileNamePostfix.${exportType.fileExtension}"
    }
}