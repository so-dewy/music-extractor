package com.dewy.musicextractish.spotify

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import org.springframework.core.io.InputStreamResource
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

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
    ): PlaylistExportResult {
        TODO("Collect all playlists ids and get request info for all of them")
    }

    fun exportPlaylists(
        authorizedClient: OAuth2AuthorizedClient,
        exportType: ExportType,
        playlistIds: List<String>
    ): PlaylistExportResult {
        val playlists = mutableListOf<Playlist>()

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
    ): Playlist? {
        val resourceUri = "${SPOTIFY_API_BASE_URL}/playlists/${id}"
        val playlist = webClient
            .get()
            .uri(resourceUri)
            .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(Playlist::class.java)
            .block()
        if (playlist?.tracks?.next != null) {
            val next = webClient
                .get()
                .uri(playlist.tracks.next)
                .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .bodyToMono(JsonNode::class.java)
                .block()
            println(next)
        }
        return playlist
    }

    private fun convertToExportType(playlists: List<Playlist>, exportType: ExportType): PlaylistExportResult {
        return when (exportType) {
            ExportType.JSON -> convertToJson(playlists)
            ExportType.CSV -> convertToCsv(playlists)
            ExportType.XLS -> convertToXls(playlists)
            ExportType.XLSX -> convertToXlsx(playlists)
        }
    }

    private fun convertToJson(playlists: List<Playlist>): PlaylistExportResult {
        val byteArray = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsBytes(playlists)
        return PlaylistExportResult(
            inputStreamResource = InputStreamResource(ByteArrayInputStream(byteArray)),
            contentLength = byteArray.size.toLong()
        )
    }

    private fun convertToXlsx(playlists: List<Playlist>): PlaylistExportResult {
        TODO("Not yet implemented")
    }

    private fun convertToXls(playlists: List<Playlist>): PlaylistExportResult {
        TODO("Not yet implemented")
    }

    private fun convertToCsv(playlists: List<Playlist>): PlaylistExportResult {
        val tracks = prepareTracks(playlists.first())

        val byteArrayOutputStream = ByteArrayOutputStream()

        val mapper = CsvMapper()
        val schema = mapper.schemaFor(TrackFlattened::class.java).withHeader()
        mapper.writer(schema).writeValue(byteArrayOutputStream, tracks)

        val byteArray = byteArrayOutputStream.toByteArray()

        return PlaylistExportResult(
            inputStreamResource = InputStreamResource(ByteArrayInputStream(byteArray)),
            contentLength = byteArray.size.toLong()
        )
    }

    private fun prepareTracks(playlist: Playlist): List<TrackFlattened> {
        val tracks = mutableListOf<TrackFlattened>()

        playlist.tracks.items.forEach {
            tracks.add(
                TrackFlattened(
                    added_at = it.added_at,
                    name = it.track.name,
                    artists = it.track.artists.foldIndexed("") { index: Int, acc: String, artist: Artist ->
                        if (index != 0) "$acc && ${artist.name}" else artist.name
                    },
                    album = it.track.album.name
                )
            )
        }

        return tracks
    }
}

data class PlaylistExportResult(val inputStreamResource: InputStreamResource, val contentLength: Long)