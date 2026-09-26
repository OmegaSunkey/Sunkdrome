package omega.sunkey.sunkdrome.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.dataclasses.PlaylistData
import omega.sunkey.sunkdrome.server.dataclasses.Playlists
import omega.sunkey.sunkdrome.server.dataclasses.PlaylistsView
import omega.sunkey.sunkdrome.server.reject
import omega.sunkey.sunkdrome.server.room.SubsonicDao
import omega.sunkey.sunkdrome.server.success
import omega.sunkey.sunkdrome.server.toIsoTime

fun getPlaylists(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    context.future(scope.future {
        val playlists = dao.getAllPlaylists()
        val playdata = mutableListOf<PlaylistData>()
        playlists.forEach {
            playdata.add(PlaylistData(
                it.id,
                it.name,
                it.comment,
                it.owner,
                it.public,
                it.songCount,
                it.duration,
                it.created?.toIsoTime(),
                it.changed?.toIsoTime()
            ))
        }
        success(context, PlaylistsView(Playlists(playdata)))
    })
}