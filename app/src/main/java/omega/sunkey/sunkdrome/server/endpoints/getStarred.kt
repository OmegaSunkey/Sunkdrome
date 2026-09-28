package omega.sunkey.sunkdrome.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.dataclasses.AlbumData
import omega.sunkey.sunkdrome.server.dataclasses.ArtistData
import omega.sunkey.sunkdrome.server.dataclasses.SongData
import omega.sunkey.sunkdrome.server.dataclasses.StarredData
import omega.sunkey.sunkdrome.server.dataclasses.Starreds1
import omega.sunkey.sunkdrome.server.dataclasses.conType
import omega.sunkey.sunkdrome.server.room.SubsonicDao
import omega.sunkey.sunkdrome.server.success
import omega.sunkey.sunkdrome.server.toIsoTime

fun getStarred(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    context.future(scope.future {
        val allStars = dao.getAllStarred()
        val songList = mutableListOf<SongData>()
        val albumList = mutableListOf<AlbumData>()
        val artistList = mutableListOf<ArtistData>()
        allStars.forEach { starred ->
            when(starred.type) {
                "song" -> {
                    val it = dao.getSongWithMeta(starred.id) ?: return@forEach
                    songList.add(
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
                            discNumber = it.song.discNumber,
                            created = it.song.dateAdded.toIsoTime(),
                            albumId = it.song.albumId,
                            artistId = it.song.artistId,
                            userRating = it.song.userRating ?: 0,
                            starred = it.song.starred?.toIsoTime()
                        )
                    )
                }
                "album" -> {
                    val it = dao.getAlbumWithSongs(starred.id) ?: return@forEach
                    var totalDuration = 0
                    it.songs.forEach { song ->
                        totalDuration += song.duration
                    }
                    albumList.add(
                        AlbumData(
                            id = it.album.id,
                            parent = it.album.artistId,
                            album = it.album.title,
                            coverArt = it.album.coverArt,
                            songCount = it.songs.size,
                            created = it.album.dateAdded.toIsoTime(),
                            duration = totalDuration,
                            artistId = it.artist.id,
                            artist = it.artist.name,
                            year = it.album.year ?: 2000,
                            userRating = it.album.userRating ?: 0,
                            starred = it.album.starred?.toIsoTime()
                        )
                    )
                }
                "artist" -> {
                    val it = dao.getArtistWithDetails(starred.id) ?: return@forEach
                    artistList.add(
                        ArtistData(
                            id = it.artist.id,
                            name = it.artist.name,
                            albumCount = it.albums.size,
                            coverArt = it.albums[0].album.coverArt,
                            userRating = it.artist.userRating ?: 0,
                            starred = it.artist.starred?.toIsoTime()
                        )
                    )
                }
            }
        }
        success(context, Starreds1(StarredData(artistList, albumList, songList)))
    })
}