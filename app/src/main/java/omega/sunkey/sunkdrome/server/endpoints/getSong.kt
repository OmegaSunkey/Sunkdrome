package omega.sunkey.sunkdrome.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.dataclasses.SongData
import omega.sunkey.sunkdrome.server.dataclasses.SongView
import omega.sunkey.sunkdrome.server.dataclasses.conType
import omega.sunkey.sunkdrome.server.reject
import omega.sunkey.sunkdrome.server.room.SubsonicDao
import omega.sunkey.sunkdrome.server.success
import omega.sunkey.sunkdrome.server.toIsoTime

fun getSong(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, 10, "Required parameter is missing.")
        return
    }
    context.future(scope.future {
        val metasong = dao.getSongWithMeta(id)
        if (metasong == null) {
            reject(context, 70, "The requested data was not found.")
            return@future
        }
        success(context, SongView(
            SongData(
                metasong.song.id,
                metasong.album.id,
                false,
                metasong.song.title,
                metasong.album.title,
                metasong.artist.name,
                metasong.song.track,
                metasong.song.year,
                metasong.song.genre,
                metasong.song.coverArt,
                metasong.song.size,
                conType(metasong.song.suffix!!),
                metasong.song.suffix,
                metasong.song.duration,
                null,
                metasong.song.path,
                null,
                metasong.song.dateAdded.toIsoTime(),
                metasong.song.albumId,
                metasong.song.artistId,
            )
        ))
    })
}