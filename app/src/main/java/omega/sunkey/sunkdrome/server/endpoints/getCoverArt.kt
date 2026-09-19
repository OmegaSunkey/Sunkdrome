package omega.sunkey.sunkdrome.server.endpoints

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext
import omega.sunkey.sunkdrome.server.reject
import omega.sunkey.sunkdrome.server.room.SubsonicDao
import omega.sunkey.sunkdrome.server.success
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun getCoverArt(context: Context, scope: CoroutineScope, dao: SubsonicDao, androidContext: android.content.Context) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, 10, "Required parameter is missing.")
        return
    }
    context.future(scope.future {
        try {
            val contentType = checkImageHeader(getCoverArt(androidContext, id, dao))
            val cover = getCoverArt(androidContext, id, dao)
            // using regular context.result(inputStream) doesn't work i dont know why, jackson explodes trying to serialize something
            if(cover != null) {
                cover.use { input -> // i asked claude and it told me to override internal result object so epic
                    context.res.outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                return404(context)
            }
        } catch (e: Exception) {
            context.status(500)
            context.result("explode")
        }
    })
}

suspend fun getCoverArt(androidContext: android.content.Context, id: String, dao: SubsonicDao): InputStream? {
    val uri = when {
        dao.getSong(id) != null -> dao.getSong(id)!!.coverArt
        dao.getAlbum(id) != null -> dao.getAlbum(id)!!.coverArt
        dao.getArtistWithDetails(id) != null -> dao.getArtistWithDetails(id)!!.albums[0].album.coverArt
        else -> null
    }
    if(uri == null) return null

    val inputStream = androidContext.contentResolver.openInputStream(Uri.parse(uri))
    if(inputStream == null) return null else return inputStream
}

fun return404(context: Context) {
    context.status(404)
    context.result("Not found")
}

fun checkImageHeader(image: InputStream?): String {
    if(image == null) return ""
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeStream(image, null, options)
    return options.outMimeType ?: "image/jpeg"
}
