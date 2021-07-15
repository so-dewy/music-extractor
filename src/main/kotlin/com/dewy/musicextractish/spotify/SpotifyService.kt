package com.dewy.musicextractish.spotify

import com.dewy.musicextractish.file.ExportType
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.io.ByteArrayInputStream

const val SPOTIFY_API_BASE_URL = "https://api.spotify.com/v1"

@Service
class SpotifyService(val webClient: WebClient) {
    fun getUserInfo(authorizedClient: OAuth2AuthorizedClient): String? {
        val resourceUri = "${SPOTIFY_API_BASE_URL}/me"
        return webClient
            .get()
            .uri(resourceUri)
            .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }

    fun getUserPlaylists(authorizedClient: OAuth2AuthorizedClient): String? {
        val resourceUri = "${SPOTIFY_API_BASE_URL}/me/playlists"
        return webClient
            .get()
            .uri(resourceUri)
            .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }

    fun exportAllPlaylists(
        authorizedClient: OAuth2AuthorizedClient,
        exportType: ExportType
    ): ResponseEntity<InputStreamResource> {
        TODO("Collect all playlists id's and get request info for all of them")
    }

    fun exportPlaylists(
        authorizedClient: OAuth2AuthorizedClient,
        exportType: ExportType,
        playlistIds: List<String>
    ): ResponseEntity<InputStreamResource> {
        val playlists = mutableListOf<String>()

        playlistIds.forEach {
            val playlist = fetchPlaylist(authorizedClient, it)
            if (playlist != null) {
                playlists.add(playlist)
            }
        }

        return convertToExportType(playlists, exportType)
    }

    fun fetchPlaylist(
        authorizedClient: OAuth2AuthorizedClient,
        id: String
    ): String? {
        val resourceUri = "${SPOTIFY_API_BASE_URL}/playlists/${id}"
        return webClient
            .get()
            .uri(resourceUri)
            .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }

    private fun convertToExportType(playlists: MutableList<String>, exportType: ExportType): ResponseEntity<InputStreamResource> {
        return when (exportType) {
            ExportType.JSON -> convertToJson(playlists)
            ExportType.CSV -> convertToCsv(playlists)
            ExportType.XLS -> convertToXls(playlists)
            ExportType.XLSX -> convertToXlsx(playlists)
        }
    }

    private fun convertToJson(playlists: MutableList<String>): ResponseEntity<InputStreamResource> {
        val byteArray = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsBytes(playlists)
        return ResponseEntity
            .ok()
            .contentLength(byteArray.size.toLong())
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=playlists.json")
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(InputStreamResource(ByteArrayInputStream(byteArray)))
    }

    private fun convertToXlsx(playlists: MutableList<String>): ResponseEntity<InputStreamResource> {
        TODO("Not yet implemented")
    }

    private fun convertToXls(playlists: MutableList<String>): ResponseEntity<InputStreamResource> {
        TODO("Not yet implemented")
    }

    private fun convertToCsv(playlists: MutableList<String>): ResponseEntity<InputStreamResource> {
        TODO("Not yet implemented")
    }
}