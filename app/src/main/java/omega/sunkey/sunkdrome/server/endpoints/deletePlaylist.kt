package omega.sunkey.sunkdrome.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.Reject
import omega.sunkey.sunkdrome.server.reject
import omega.sunkey.sunkdrome.server.room.SubsonicDao
import omega.sunkey.sunkdrome.server.success

fun deletePlaylist(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val playlistId = context.queryParam("playlistId")
    if (playlistId == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.future(scope.future {
        dao.deletePlaylist(playlistId)
        success(context, null)
    })
}