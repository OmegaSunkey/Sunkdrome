package omega.sunkey.sunkdrome.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.dataclasses.AlbumData
import omega.sunkey.sunkdrome.server.dataclasses.AlbumView
import omega.sunkey.sunkdrome.server.reject
import omega.sunkey.sunkdrome.server.room.SubsonicDao
import omega.sunkey.sunkdrome.server.success

fun getAlbum(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, 10, "Required parameter is missing.")
        return
    }
    context.future(scope.future {
        val metaalbum = dao.getAlbumWithSongs(id)
        if (metaalbum == null) {
            reject(context, 70, "The requested data was not found.")
            return@future
        }
        val artist = dao.getArtist(metaalbum.album.artistId!!)
        var totalDuration = 0
        metaalbum.songs.forEach { it ->
            totalDuration += it.duration
        }
        success(context, AlbumView(
            AlbumData(
                metaalbum.album.id,
                metaalbum.album.title,
                artist!!.name,
                metaalbum.songs.size,
                totalDuration,
                metaalbum.songs
            )
        ))
    })
}