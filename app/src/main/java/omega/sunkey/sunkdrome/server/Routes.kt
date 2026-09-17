package omega.sunkey.sunkdrome.server

import android.content.Context
import io.javalin.Javalin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.dataclasses.Directory
import omega.sunkey.sunkdrome.server.dataclasses.DirectoryChild
import omega.sunkey.sunkdrome.server.dataclasses.License
import omega.sunkey.sunkdrome.server.dataclasses.LicenseView
import omega.sunkey.sunkdrome.server.dataclasses.MusicFolder
import omega.sunkey.sunkdrome.server.dataclasses.MusicFolders
import omega.sunkey.sunkdrome.server.dataclasses.MusicFoldersView
import omega.sunkey.sunkdrome.server.dataclasses.OpenSubsonicExtensionsPayload
import omega.sunkey.sunkdrome.server.dataclasses.conType
import omega.sunkey.sunkdrome.server.room.Song
import omega.sunkey.sunkdrome.server.room.SubsonicDatabase
import java.net.URLDecoder
import java.net.URLEncoder

fun Javalin.setBeforeHandlers() {
    this.before("/rest/*") { ctx ->
        //Here goes parameter checking
        /*Implement:
        - Query u: username -string
        - Query p: password -string
        - Query t: token -md5-string
        - Query s: salt -string
        - Validate all parameters and return if u & p are missing or are wrong
         */
        val u = ctx.queryParam("u")
        val p = ctx.queryParam("p")
        //val t: String? = ctx.queryParam("t")
        //val s: String? = ctx.queryParam("s")

        //TODO: actual user management
        if(u == "sunkey" && p == "sunkey") {
            ctx.attribute("currentUser", u)
        } else {
            reject(ctx, 40, "Wrong username or password")
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
        ctx.future(scope.future {
            val all_paths = dao.getAllSongPaths()
            val dc_paths = ArrayList<MusicFolder>()
            for (path in all_paths) {
                dc_paths.add(MusicFolder(path, path.substringAfterLast("/")))
            }
            success(ctx, MusicFoldersView(MusicFolders(dc_paths.toList())))
        })
    }

    this.get("/rest/getMusicDirectory.view") { ctx ->
        val id = ctx.queryParam("id") ?: return@get
        ctx.future(scope.future {
            val path = URLDecoder.decode(id, "UTF-8")
            val allSongs = dao.getSongsByPath(path)
            val songs = mutableListOf<Song>()
            val subdirs = mutableSetOf<String>()
            for (song in allSongs) {
                val relPath = song.path.removePrefix(path).removePrefix("/")
                if(relPath.contains("/")) {
                    val whichsubdir = relPath.substringBefore("/")
                    subdirs.add(whichsubdir)
                } else {
                    songs.add(song)
                }
            }
            val children = mutableListOf<DirectoryChild>()
            for(subdir in subdirs) {
                val subdirPath = "$path/$subdir"
                children.add(
                    DirectoryChild(
                        URLEncoder.encode(subdirPath, "UTF-8"),
                        name = subdir,
                        isDir = true
                    )
                )
            }
            for(song in songs) {
                children.add(
                    DirectoryChild(
                        song.id,
                        URLEncoder.encode(path, "UTF-8"),
                        song.title,
                        null,
                        null,
                        null,
                        false,
                        song.duration,
                        null,
                        song.size,
                        song.suffix,
                        conType(song.suffix!!),
                        song.path,
                        song.dateAdded
                    )
                )
            }
            val dir = Directory(
                id,
                path.substringAfterLast("/"),
                children
            )
        })
    }
}