package omega.sunkey.sunkdrome.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.dataclasses.AlbumsWithoutSongs
import omega.sunkey.sunkdrome.server.dataclasses.ArtistData
import omega.sunkey.sunkdrome.server.dataclasses.Index
import omega.sunkey.sunkdrome.server.dataclasses.Indexes
import omega.sunkey.sunkdrome.server.dataclasses.IndexesView
import omega.sunkey.sunkdrome.server.room.SubsonicDao
import omega.sunkey.sunkdrome.server.success
import kotlin.collections.set

fun getIndexes(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val musicFolderId = context.queryParam("musicFolderId")
    context.future(scope.future {
        val ignoredArticles = listOf("The", "El", "La", "Los", "Las")
        val allArtists = dao.getAllArtistsWithMeta()
        val allAlbums = dao.getAllAlbumsWithMeta()
        val indexArtistMap = mutableMapOf<String, MutableList<ArtistData>>()
        val indexAlbumMap = mutableMapOf<String, MutableList<AlbumsWithoutSongs>>()
        val finalIndex = mutableListOf<Index>()
        for(metaartist in allArtists) {
            val fChar = getFirstChar(metaartist.artist.name, ignoredArticles)
            if(indexArtistMap.containsKey(fChar)) {
                indexArtistMap[fChar]!!.add(
                    ArtistData(
                        metaartist.artist.id,
                        metaartist.artist.name,
                        metaartist.albums.size,
                        metaartist.albums[0].album.coverArt
                    )
                )
            } else {
                indexArtistMap[fChar] = mutableListOf(
                    ArtistData(
                        metaartist.artist.id,
                        metaartist.artist.name,
                        metaartist.albums.size,
                        metaartist.albums[0].album.coverArt
                    )
                )
            }
        }
        for(metaalbum in allAlbums) {
            val fChar = getFirstChar(metaalbum.album.title, ignoredArticles)
            var totalDuration = 0
            metaalbum.songs.forEach {
                totalDuration += it.duration
            }
            if(indexAlbumMap.containsKey(fChar)) {
                indexAlbumMap[fChar]!!.add(
                    AlbumsWithoutSongs(
                        metaalbum.album.id,
                        metaalbum.album.title,
                        metaalbum.artist.name,
                        metaalbum.songs.size,
                        totalDuration,
                        metaalbum.album.coverArt
                    )
                )
            } else {
                indexAlbumMap[fChar] = mutableListOf(
                    AlbumsWithoutSongs(
                        metaalbum.album.id,
                        metaalbum.album.title,
                        metaalbum.artist.name,
                        metaalbum.songs.size,
                        totalDuration,
                        metaalbum.album.coverArt
                    )
                )
            }
        }
        for(index in (indexArtistMap.keys + indexAlbumMap.keys).distinct().sorted()) {
            finalIndex.add(
                Index(
                    index,
                    indexArtistMap[index].orEmpty(),
                    indexAlbumMap[index].orEmpty()
                )
            )
        }
        success(context, IndexesView(Indexes(ignoredArticles, finalIndex)))
    })
}