package omega.sunkey.sunkdrome.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.Reject
import omega.sunkey.sunkdrome.server.reject
import omega.sunkey.sunkdrome.server.room.Starred
import omega.sunkey.sunkdrome.server.room.SubsonicDao
import omega.sunkey.sunkdrome.server.success
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun star(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
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
        val star = when {
            id != null -> {
                Starred(id, "song", Clock.System.now().epochSeconds)
            }
            artistId != null -> {
                Starred(artistId, "artist", Clock.System.now().epochSeconds)
            }
            albumId != null -> {
                Starred(albumId, "artist", Clock.System.now().epochSeconds)
            }
            else -> null
        }
        if (star != null) {
            dao.addStar(star)
            success(context, null)
        } else {
            reject(context, Reject.GENERIC)
            return@future
        }
    })
}