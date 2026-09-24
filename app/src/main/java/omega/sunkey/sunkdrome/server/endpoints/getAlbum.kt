package omega.sunkey.sunkdrome.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.Reject
import omega.sunkey.sunkdrome.server.dataclasses.AlbumData
import omega.sunkey.sunkdrome.server.dataclasses.AlbumView
import omega.sunkey.sunkdrome.server.dataclasses.SongData
import omega.sunkey.sunkdrome.server.dataclasses.conType
import omega.sunkey.sunkdrome.server.reject
import omega.sunkey.sunkdrome.server.room.SubsonicDao
import omega.sunkey.sunkdrome.server.success
import omega.sunkey.sunkdrome.server.toIsoTime

fun getAlbum(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.future(scope.future {
        val metaalbum = dao.getAlbumWithSongs(id)
        val songList = mutableListOf<SongData>()
        if (metaalbum == null) {
            reject(context, Reject.NODATA)
            return@future
        }
        var totalDuration = 0
        metaalbum.songs.forEach { it ->
            totalDuration += it.duration
            songList.add(SongData(
                id = it.id,
                parent = it.albumId,
                title = it.title,
                album = metaalbum.album.title,
                artist = metaalbum.artist.name,
                track = it.track,
                year = it.year,
                genre = it.genre,
                coverArt = it.coverArt,
                size = it.size,
                contentType = conType(it.suffix),
                suffix = it.suffix,
                duration = it.duration,
                bitRate = it.bitrate,
                path = it.path,
                created = it.dateAdded.toIsoTime(),
                albumId = it.albumId,
                artistId = it.artistId
            ))
        }
        success(context, AlbumView(
            AlbumData(
                metaalbum.album.id,
                metaalbum.album.title,
                metaalbum.artist.name
                metaalbum.songs.size,
                totalDuration,
                metaalbum.album.coverArt
                songList,
            )
        ))
    })
}