package omega.sunkey.sunkdrome.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.dataclasses.ArtistData
import omega.sunkey.sunkdrome.server.dataclasses.ArtistIndex
import omega.sunkey.sunkdrome.server.dataclasses.Artists
import omega.sunkey.sunkdrome.server.dataclasses.ArtistsView
import omega.sunkey.sunkdrome.server.room.SubsonicDao
import omega.sunkey.sunkdrome.server.success

fun getArtists(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val ignoredArticles = listOf("The", "El", "La", "Los", "Las")
    context.future(scope.future{
        val allArtists = dao.getAllArtistsWithMeta()
        val artistIndex = mutableListOf<ArtistIndex>()
        val artistMap = mutableMapOf<String, MutableList<ArtistData>>()
        for(metaartist in allArtists) {
            val fChar = getFirstChar(metaartist.artist.name, ignoredArticles)
            if(artistMap.containsKey(fChar)) {
                artistMap[fChar]!!.add(
                    ArtistData(
                        metaartist.artist.id,
                        metaartist.artist.name,
                        metaartist.albums.size,
                        metaartist.albums[0].album.coverArt
                    )
                )
            } else {
                artistMap[fChar] = mutableListOf<ArtistData>(
                    ArtistData(
                        metaartist.artist.id,
                        metaartist.artist.name,
                        metaartist.albums.size,
                        metaartist.albums[0].album.coverArt
                    )
                )
            }
        }
        for (artist in artistMap) {
            artistIndex.add(ArtistIndex(
                artist.key,
                artist.value
            ))
        }
        success(context, ArtistsView(Artists(ignoredArticles, artistIndex)))
    })
}

fun getFirstChar(name: String, igArt: List<String>): String {
    if(name.isEmpty()) return "#"

    var char = name.lowercase()
    for(art in igArt) {
        if(char.startsWith("$art ", ignoreCase = true)) {
            char = char.removePrefix("${art.lowercase()} ")
        }
    }
    char = char.first().uppercase()

    return if (!char.first().isLetter()) {
        "#"
    } else {
        char
    }
}