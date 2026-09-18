package omega.sunkey.sunkdrome.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.dataclasses.AlbumsWithoutSongs
import omega.sunkey.sunkdrome.server.dataclasses.SingleArtist
import omega.sunkey.sunkdrome.server.dataclasses.SingleArtistView
import omega.sunkey.sunkdrome.server.room.SubsonicDao
import omega.sunkey.sunkdrome.server.success

fun getArtist(id: String, context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    context.future(scope.future {
        val metaartist = dao.getArtistWithDetails(id)
        val albums = mutableListOf<AlbumsWithoutSongs>()
        metaartist!!.albums.forEach {
            albums.add(AlbumsWithoutSongs(
                it.album.id,
                it.album.title,
                metaartist.artist.name,
                it.songs.size,
                null
            ))
        }
        success(context, SingleArtistView(
            SingleArtist(
                metaartist.artist.name,
                metaartist.artist.name,
                metaartist.albums.size,
                albums
            )))
    })
}