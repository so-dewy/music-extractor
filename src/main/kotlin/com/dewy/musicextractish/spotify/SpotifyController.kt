package com.dewy.musicextractish.spotify

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
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
        response: HttpServletResponse,
        @RegisteredOAuth2AuthorizedClient("spotify") authorizedClient: OAuth2AuthorizedClient,
        @RequestParam exportType: ExportType,
        @RequestParam(required = false) ids: List<String>?,
        @RequestParam(required = false) selectAll: Boolean?
    ) {
        if (selectAll == true) {
            val playlistExportResults = spotifyService.exportAllPlaylists(authorizedClient, exportType)

            createArchiveResponse(response, playlistExportResults, exportType)
        }

        if (ids == null || ids.isEmpty()) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return
        }

        val playlistExportResults = spotifyService.exportPlaylists(authorizedClient, exportType, ids)

        createArchiveResponse(response, playlistExportResults, exportType)
    }

    @CrossOrigin("http://localhost:3000", allowCredentials = "true")
    @GetMapping("/spotify/user/playlists")
    fun getUserPlaylists(
        @RegisteredOAuth2AuthorizedClient("spotify") authorizedClient: OAuth2AuthorizedClient,
        @RequestParam offset: Int,
        @RequestParam limit: Int,
    ): ResponseEntity<String?> {
        if (offset < 0 || limit < 0) throw IllegalArgumentException("Offset or limit is negative")

        val playlists = spotifyService.getUserPlaylists(authorizedClient, offset, limit)

        return if (playlists != null) ResponseEntity.ok(playlists) else throw IllegalStateException("Playlists not found")
    }

    @CrossOrigin("http://localhost:3000", allowCredentials = "true")
    @GetMapping("/spotify/user")
    fun getUserInfo(
        @RegisteredOAuth2AuthorizedClient("spotify") authorizedClient: OAuth2AuthorizedClient
    ): String? {
        return spotifyService.getUserInfo(authorizedClient)
    }

    private fun createArchiveResponse(
        response: HttpServletResponse,
        playlistExportResults: List<PlaylistExportResult>,
        exportType: ExportType
    ) {
        response.status = HttpServletResponse.SC_OK
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${generateArchiveName()}")
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
        response.contentType = MediaType.APPLICATION_OCTET_STREAM.toString()

        val zipOutStream = ZipOutputStream(response.outputStream)

        playlistExportResults.forEach {
            val zipEntry = ZipEntry("${it.playlistName}.${exportType.fileExtension}")
            zipEntry.size = it.contentLength

            zipOutStream.putNextEntry(zipEntry)

            StreamUtils.copy(it.inputStreamResource.inputStream, zipOutStream)
            zipOutStream.closeEntry()
        }

        zipOutStream.finish()
    }

    private fun generateArchiveName(): String {
        val date = LocalDateTime.now()
        val fileNamePostfix = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))

        return "playlists_$fileNamePostfix.zip"
    }
}