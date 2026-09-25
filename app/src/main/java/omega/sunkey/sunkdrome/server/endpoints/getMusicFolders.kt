package omega.sunkey.sunkdrome.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.dataclasses.MusicFolder
import omega.sunkey.sunkdrome.server.dataclasses.MusicFolders
import omega.sunkey.sunkdrome.server.dataclasses.MusicFoldersView
import omega.sunkey.sunkdrome.server.room.SubsonicDao
import omega.sunkey.sunkdrome.server.success

fun getMusicFolders(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    context.future(scope.future {
        val allpaths = dao.getAllSongPaths()
        val dcpaths = mutableMapOf<Int, MusicFolder>()
        for (path in allpaths) {
            val key = path.substringBeforeLast("/").substringAfterLast("/").hashCode()
            if(!dcpaths.containsKey(key)) {
                dcpaths[key] = MusicFolder(key, path.substringBeforeLast("/").substringAfterLast("/"))
            }
        }
        success(context, MusicFoldersView(MusicFolders(dcpaths.values.toList())))
    })
}