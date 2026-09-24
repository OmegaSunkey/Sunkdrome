package omega.sunkey.sunkdrome.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.Reject
import omega.sunkey.sunkdrome.server.dataclasses.AlbumsWithoutSongs
import omega.sunkey.sunkdrome.server.dataclasses.ArtistData
import omega.sunkey.sunkdrome.server.dataclasses.Search
import omega.sunkey.sunkdrome.server.dataclasses.SearchView
import omega.sunkey.sunkdrome.server.dataclasses.SongData
import omega.sunkey.sunkdrome.server.dataclasses.conType
import omega.sunkey.sunkdrome.server.reject
import omega.sunkey.sunkdrome.server.room.SubsonicDao
import omega.sunkey.sunkdrome.server.success
import omega.sunkey.sunkdrome.server.toIsoTime
import kotlin.math.min

fun search3(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val query = context.queryParam("query")
    if (query == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    val artistCount = if(context.queryParam("artistCount") != null) min(context.queryParam("artistCount")!!.toInt(), 40) else 20
    val albumCount = if(context.queryParam("albumCount") != null) min(context.queryParam("albumCount")!!.toInt(), 40) else 20
    val songCount = if(context.queryParam("songCount") != null) min(context.queryParam("songCount")!!.toInt(), 40) else 20

    val artistOffset = context.queryParam("artistOffset")?.toIntOrNull() ?: 0
    val albumOffset = context.queryParam("albumOffset")?.toIntOrNull() ?: 0
    val songOffset = context.queryParam("songOffset")?.toIntOrNull() ?: 0

    context.future(scope.future {
        val artists = dao.searchArtists(query, artistCount, artistOffset)
        val albums = dao.searchAlbums(query, albumCount, albumOffset)
        val songs = dao.searchSongs(query, songCount, songOffset)
        val metaartist = mutableListOf<ArtistData>()
        val metaalbum = mutableListOf<AlbumsWithoutSongs>()
        val metasong = mutableListOf<SongData>()
        artists.forEach {
            metaartist.add(
                ArtistData(it.artist.id, it.artist.name, it.albums.size, it.albums[0].album.coverArt)
            )
        }
        albums.forEach {
            var totalDuration = 0
            it.songs.forEach {
                totalDuration += it.duration
            }
            metaalbum.add(
                AlbumsWithoutSongs(
                    it.album.id,
                    title = it.album.title,
                    artist = it.artist.name,
                    songCount = it.songs.size,
                    duration = totalDuration,
                    coverArt = it.album.coverArt
                )
            )
        }
        songs.forEach {
            metasong.add(
                SongData(
                    id = it.song.id,
                    parent = it.album.id,
                    isDir = false,
                    title = it.song.title,
                    album = it.album.title,
                    artist = it.artist.name,
                    track = it.song.track,
                    year = it.song.year,
                    genre = it.song.genre,
                    coverArt = it.song.coverArt,
                    size = it.song.size,
                    contentType = conType(it.song.suffix!!),
                    suffix = it.song.suffix,
                    duration = it.song.duration,
                    bitRate = it.song.bitrate,
                    path = it.song.path,
                    discNumber = null,
                    created = it.song.dateAdded.toIsoTime(),
                    albumId = it.song.albumId,
                    artistId = it.song.artistId
                )
            )
        }
        success(context, SearchView(Search(metaartist, metaalbum, metasong)))
    })
}