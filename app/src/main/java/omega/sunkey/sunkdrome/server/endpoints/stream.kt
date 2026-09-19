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
        val song = dao.getSongWithMeta(id)
        if(song == null) {
            reject(context, 70, "The requested content was not found.")
            return@future
        }
        val range = parseRangeHeader(context.header("Range"), song.song.size.toInt())
        sendStream(context, song.song.id, conType(song.song.suffix!!), song.song.size, range, androidContext)
    })
}

fun sendStream(context: Context, id: String, mime: String, size: Long, range: Pair<Int, Int>?, androidContext: android.content.Context) {
    context.contentType(mime)

    val uri = ContentUris.withAppendedId(
        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
        id.toLong()
    )
    val inputStream = androidContext.contentResolver.openInputStream(uri)
    if (inputStream != null && range != null) {
        val buffer = ByteArray(range.second - range.first)
        inputStream.read(buffer, range.first, range.second)
        context.header("Content-Length", buffer.size.toString())
        context.header("Content-Range", "bytes ${range.first}-${range.second}/$size")
        context.status(206)
        context.result(buffer)
    } else if (inputStream != null) {
        context.header("Content-Length", size.toString())
        context.result(inputStream)
    }
}

fun parseRangeHeader(range: String?, fileSize: Int): Pair<Int, Int>? {
    if (range == null) return null
    val rangeStart = range.removePrefix("bytes=").split("-")[0].toIntOrNull()
    val rangeEnd = range.removePrefix("bytes=").split("-")[0].toIntOrNull()

    return when {
        rangeStart == null && rangeEnd != null -> Pair(maxOf(0, fileSize - rangeEnd), fileSize - 1)
        rangeStart != null && rangeEnd == null && rangeStart >= fileSize -> Pair(rangeStart, fileSize)
        rangeStart != null && rangeEnd != null && rangeStart >= fileSize -> Pair(rangeStart, minOf(rangeEnd, fileSize - 1))
        else -> null
    }
}