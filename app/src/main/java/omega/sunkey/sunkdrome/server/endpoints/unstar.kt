package omega.sunkey.sunkdrome.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.Reject
import omega.sunkey.sunkdrome.server.reject
import omega.sunkey.sunkdrome.server.room.SubsonicDao
import omega.sunkey.sunkdrome.server.success

fun unstar(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val id = context.queryParam("id")
    val albumId = context.queryParam("albumId")
    val artistId = context.queryParam("artistId")
    when {
        id != null && albumId != null && artistId != null ||
                id != null && albumId != null ||
                id != null && artistId != null ||
                albumId != null && artistId != null -> {
            reject(context, Reject.GENERIC)
            return
        }
        id == null && albumId == null && artistId == null -> {
            reject(context, Reject.MISSINGPARAM)
            return
        }
    }
    context.future(scope.future {
        when {
            id != null -> dao.removeStar(id).also {
                dao.updateStarredSong(id, null)
            }
            albumId != null -> dao.removeStar(albumId).also {
                dao.updateStarredAlbum(albumId, null)
            }
            artistId != null -> dao.removeStar(artistId).also {
                dao.updateStarredArtist(artistId, null)
            }
        }
        success(context, null)
    })
}