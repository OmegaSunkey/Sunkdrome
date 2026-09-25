package omega.sunkey.sunkdrome.server

import android.content.Context
import android.util.Log
import io.javalin.Javalin
import kotlinx.coroutines.CoroutineScope
import omega.sunkey.sunkdrome.server.dataclasses.License
import omega.sunkey.sunkdrome.server.dataclasses.LicenseView
import omega.sunkey.sunkdrome.server.dataclasses.OpenSubsonicExtensionsPayload
import omega.sunkey.sunkdrome.server.endpoints.getAlbum
import omega.sunkey.sunkdrome.server.endpoints.getArtist
import omega.sunkey.sunkdrome.server.endpoints.getArtists
import omega.sunkey.sunkdrome.server.endpoints.getCoverArt
import omega.sunkey.sunkdrome.server.endpoints.getIndexes
import omega.sunkey.sunkdrome.server.endpoints.getMusicDirectory
import omega.sunkey.sunkdrome.server.endpoints.getMusicFolders
import omega.sunkey.sunkdrome.server.endpoints.getSong
import omega.sunkey.sunkdrome.server.endpoints.search3
import omega.sunkey.sunkdrome.server.endpoints.stream
import omega.sunkey.sunkdrome.server.room.SubsonicDatabase
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun Javalin.setBeforeHandlers() {
    this.before("/rest/*") { ctx ->
        val u = ctx.queryParam("u")
        val p = ctx.queryParam("p")
        //val t: String? = ctx.queryParam("t")
        //val s: String? = ctx.queryParam("s")

        //TODO: actual user management
        if(u == null && p == null) {
            reject(ctx, Reject.MISSINGPARAM)
        } else if(u != "sunkey" && p != "sunkey") {
            reject(ctx, Reject.WRONGAUTH)
        } else ctx.attribute("currentUser", u)
    }
}

fun Javalin.setPaths(context: Context, scope: CoroutineScope) {
    val db = SubsonicDatabase.getInstance(context)
    val dao = db.subsonicDao()

    this.get("/rest/ping*") { ctx ->
        success(ctx, null)
    }

    this.get("/rest/getLicense*") { ctx ->
        success(ctx, LicenseView(License()))
    }

    this.get("/rest/getOpenSubsonicExtensions*") { ctx ->
        success(ctx, OpenSubsonicExtensionsPayload(emptyList()))
    }

    this.get("/rest/getMusicFolders*") { ctx ->
        getMusicFolders(ctx, scope, dao)
    }

    this.get("/rest/getMusicDirectory*") { ctx ->
        getMusicDirectory(ctx, scope, dao)
    }

    this.get("/rest/getArtists*") { ctx ->
        getArtists(ctx, scope, dao)
    }

    this.get("/rest/getArtist*") { ctx ->
        getArtist(ctx, scope, dao)
    }

    this.get("/rest/getAlbum*") { ctx ->
        getAlbum(ctx, scope, dao)
    }

    this.get("/rest/getSong*") { ctx ->
        getSong(ctx, scope, dao)
    }

    this.get("/rest/getIndexes*") { ctx ->
        getIndexes(ctx, scope, dao)
    }

    this.get("/rest/search3*") { ctx ->
        search3(ctx, scope, dao)
    }

    this.get("/rest/stream*") { ctx ->
        stream(ctx, scope, dao, context)
    }

    this.get("/rest/getCoverArt*") { ctx ->
        getCoverArt(ctx, scope, dao, context)
    }

    this.error(404) { ctx ->
        reject(ctx, Reject.NODATA)
        Log.i("Routes", "Client requested route: ${ctx.fullUrl()}")
    }

    this.after("/*") { ctx ->
        Log.i("Routes", "200; Client requested: ${ctx.fullUrl()}")
    }
}

fun Long.toIsoTime(): String {
    return Instant.ofEpochSecond(this).atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(this.toByteArray())
    return digest.joinToString("") { "%02x".format(it) }
}
