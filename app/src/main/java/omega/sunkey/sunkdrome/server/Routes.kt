package omega.sunkey.sunkdrome.server

import android.content.Context
import io.javalin.Javalin
import kotlinx.coroutines.CoroutineScope
import omega.sunkey.sunkdrome.server.dataclasses.License
import omega.sunkey.sunkdrome.server.dataclasses.LicenseView
import omega.sunkey.sunkdrome.server.dataclasses.OpenSubsonicExtensionsPayload
import omega.sunkey.sunkdrome.server.endpoints.getAlbum
import omega.sunkey.sunkdrome.server.endpoints.getArtist
import omega.sunkey.sunkdrome.server.endpoints.getArtists
import omega.sunkey.sunkdrome.server.endpoints.getIndexes
import omega.sunkey.sunkdrome.server.endpoints.getMusicDirectory
import omega.sunkey.sunkdrome.server.endpoints.getMusicFolders
import omega.sunkey.sunkdrome.server.endpoints.getSong
import omega.sunkey.sunkdrome.server.endpoints.search3
import omega.sunkey.sunkdrome.server.endpoints.stream
import omega.sunkey.sunkdrome.server.room.SubsonicDatabase
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
        if(u == "sunkey" && p == "sunkey") {
            ctx.attribute("currentUser", u)
        } else {
            reject(ctx, 40, "Wrong username or password.")
        }
    }
}

fun Javalin.setPaths(context: Context, scope: CoroutineScope) {
    val db = SubsonicDatabase.getInstance(context)
    val dao = db.subsonicDao()

    this.get("/rest/ping.view") { ctx ->
        success(ctx, null)
    }

    this.get("/rest/getLicense.view") { ctx ->
        success(ctx, LicenseView(License()))
    }

    this.get("/rest/getOpenSubsonicExtensions.view") { ctx ->
        success(ctx, OpenSubsonicExtensionsPayload(emptyList()))
    }

    this.get("/rest/getMusicFolders.view") { ctx ->
        getMusicFolders(ctx, scope, dao)
    }

    this.get("/rest/getMusicDirectory.view") { ctx ->
        getMusicDirectory(ctx, scope, dao)
    }

    this.get("/rest/getArtists.view") { ctx ->
        getArtists(ctx, scope, dao)
    }

    this.get("/rest/getArtist.view") { ctx ->
        getArtist(ctx, scope, dao)
    }

    this.get("/rest/getAlbum.view") { ctx ->
        getAlbum(ctx, scope, dao)
    }

    this.get("/rest/getSong.view") { ctx ->
        getSong(ctx, scope, dao)
    }

    this.get("/rest/getIndexes.view") { ctx ->
        getIndexes(ctx, scope, dao)
    }
    this.get("/rest/search3.view") { ctx ->
        search3(ctx, scope, dao)
    }

    this.get("/rest/stream.view") { ctx ->
        stream(ctx, scope, dao, context)
    }
}

fun Long.toIsoTime(): String {
    return Instant.ofEpochSecond(this).atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}