package omega.sunkey.sunkdrome.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.Reject
import omega.sunkey.sunkdrome.server.dataclasses.AlbumData
import omega.sunkey.sunkdrome.server.dataclasses.SingleArtist
import omega.sunkey.sunkdrome.server.dataclasses.SingleArtistView
import omega.sunkey.sunkdrome.server.reject
import omega.sunkey.sunkdrome.server.room.SubsonicDao
import omega.sunkey.sunkdrome.server.success
import omega.sunkey.sunkdrome.server.toIsoTime

fun getArtist(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.future(scope.future {
        val metaartist = dao.getArtistWithDetails(id)
        if (metaartist == null) {
            reject(context, Reject.NODATA)
            return@future
        }
        val albums = mutableListOf<AlbumData>()
        metaartist.albums.forEach { it ->
            var totalDuration = 0
            it.songs.forEach { s ->
                totalDuration += s.duration
            }
            albums.add(AlbumData(
                id = it.album.id,
                parent = it.album.artistId,
                album = it.album.title,
                title = it.album.title,
                name = it.album.title,
                coverArt = it.album.coverArt,
                songCount = it.songs.size,
                created = it.album.dateAdded.toIsoTime(),
                duration = totalDuration,
                artistId = metaartist.artist.id,
                artist = metaartist.artist.name,
                year = it.album.year ?: 2000,
            ))
        }
        success(context, SingleArtistView(
            SingleArtist(
                metaartist.artist.name,
                metaartist.artist.name,
                metaartist.albums[0].album.coverArt,
                metaartist.albums.size,
                0,
                "http://${context.host()}/rest/getCoverArt.view?id=${metaartist.albums[0].album.coverArt}&u=sunkey&p=sunkey", //remember to remove hardcoded creds
                album = albums
            )))
    })
}