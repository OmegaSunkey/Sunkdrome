package omega.sunkey.sunkdrome.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.dataclasses.Directories
import omega.sunkey.sunkdrome.server.dataclasses.Directory
import omega.sunkey.sunkdrome.server.dataclasses.DirectoryChild
import omega.sunkey.sunkdrome.server.dataclasses.conType
import omega.sunkey.sunkdrome.server.reject
import omega.sunkey.sunkdrome.server.room.SongWithMetadata
import omega.sunkey.sunkdrome.server.room.SubsonicDao
import omega.sunkey.sunkdrome.server.success
import omega.sunkey.sunkdrome.server.toIsoTime
import java.net.URLDecoder
import java.net.URLEncoder

fun getMusicDirectory(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, 10, "Required parameter is missing.")
        return
    }
    context.future(scope.future {
        val path = URLDecoder.decode(id, "UTF-8")
        val allSongs = dao.getSongsByPath(path)
        if(allSongs.isEmpty()) {
            reject(context, 70, "The requested data was not found.")
            return@future
        }
        val songs = mutableListOf<SongWithMetadata>()
        val subdirs = mutableSetOf<String>()
        for (metasong in allSongs) {
            val relPath = metasong.song.path.removePrefix(path).removePrefix("/")
            if(relPath.contains("/")) {
                val whichsubdir = relPath.substringBefore("/")
                subdirs.add(whichsubdir)
            } else {
                songs.add(metasong)
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
        for(metasong in songs) {
            children.add(
                DirectoryChild(
                    metasong.song.id,
                    URLEncoder.encode(path, "UTF-8"),
                    metasong.song.title,
                    null,
                    metasong.album.title,
                    metasong.artist.name,
                    false,
                    metasong.song.duration,
                    null,
                    metasong.song.size,
                    metasong.song.suffix,
                    conType(metasong.song.suffix!!),
                    metasong.song.path,
                    metasong.song.dateAdded.toIsoTime()
                )
            )
        }
        val dir = Directory(
            id,
            path.substringAfterLast("/"),
            children
        )
        success(context, Directories(dir))
    })
}