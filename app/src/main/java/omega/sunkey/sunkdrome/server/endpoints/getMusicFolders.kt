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
        val dcpaths = ArrayList<MusicFolder>()
        for (path in allpaths) {
            dcpaths.add(MusicFolder(path, path.substringAfterLast("/")))
        }
        success(context, MusicFoldersView(MusicFolders(dcpaths.toList())))
    })
}