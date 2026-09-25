package omega.sunkey.sunkdrome.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.dataclasses.AlbumData
import omega.sunkey.sunkdrome.server.dataclasses.AlbumListView2
import omega.sunkey.sunkdrome.server.dataclasses.AlbumListViewNew
import omega.sunkey.sunkdrome.server.room.SubsonicDao
import omega.sunkey.sunkdrome.server.success
import omega.sunkey.sunkdrome.server.toIsoTime

fun getAlbumList2(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    //val type = context.queryParam("type") // lets ignore for now
    context.future(scope.future {
        val allAlbums = dao.getAllAlbumsWithMeta()
        val albumList = mutableListOf<AlbumData>()
        allAlbums.forEach {
            var totalDuration = 0
            it.songs.forEach {
                totalDuration += it.duration
            }
            albumList.add(
                AlbumData(
                    id = it.album.id,
                    parent = it.album.artistId,
                    album = it.album.title,
                    coverArt = it.album.coverArt,
                    songCount = it.songs.size,
                    created = it.album.dateAdded.toIsoTime(),
                    duration = totalDuration,
                    artistId = it.artist.id,
                    artist = it.artist.name,
                    year = it.album.year ?: 2000
                )
            )
        }
        success(context, AlbumListViewNew(AlbumListView2(albumList)))
    })
}
