package com.dewy.musicextractish.spotify

data class Playlist(
    val name: String?,
    val description: String?,
    val uri: String?,
    val href: String?,
    val id: String?,
    val snapshot_id: String?,
    val tracks: Tracks?,
)

data class Tracks(
    val href: String?,
    val limit: Int?,
    val next: String?,
    val offset: Int?,
    val previous: String?,
    val total: Int?,
    val items: MutableList<TracksItem>?
)

data class TracksItem(val added_at: String?, val track: Track?)

data class Track(
    val album: Album?,
    val artists: List<Artist>?,
    val href: String?,
    val id: String?,
    val name: String?,
    val preview_url: String?,
    val uri: String?
)

data class Album(val name: String?, val uri: String?)

data class Artist(val id: String?, val name: String?)

data class TrackFlattened(
    val name: String?,
    val artists: String?,
    val album: String?,
    val added_at: String?,
)
