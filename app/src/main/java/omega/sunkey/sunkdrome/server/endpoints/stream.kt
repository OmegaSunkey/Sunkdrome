package omega.sunkey.sunkdrome.server.endpoints

import android.content.ContentUris
import android.provider.MediaStore
import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.dataclasses.conType
import omega.sunkey.sunkdrome.server.reject
import omega.sunkey.sunkdrome.server.room.SubsonicDao

fun stream(context: Context, scope: CoroutineScope, dao: SubsonicDao, androidContext: android.content.Context) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, 10, "Required parameter is missing.")
        return
    }
    context.future(scope.future {
        val song = dao.getSongById(id)
        if(song == null) {
            reject(context, 70, "The requested content was not found.")
            return@future
        }
        sendStream(context, song.song.id, conType(song.song.suffix!!), song.song.size, androidContext)
    })
}

fun sendStream(context: Context, id: String, mime: String, size: Long, androidContext: android.content.Context) {
    context.contentType(mime)
    context.header("Content-Length", size.toString())

    val uri = ContentUris.withAppendedId(
        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
        id.toLong()
    )
    val inputStream = androidContext.contentResolver.openInputStream(uri)
    if (inputStream != null) {
        context.result(inputStream)
    }
}