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
    /*context.future(scope.future {
        val allArtists = dao.getAllArtistsWithMeta()
        val artistIndex = mutableListOf<ArtistIndex>()
        val lmao = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#"
        for (char in lmao) { //this does NOT cut it
            val artists = if(char == '#') allArtists.filter { it.artist.name[0] != char } else allArtists.filter { it.artist.name[0] == char }
            val data = mutableListOf<ArtistData>()
            artists.forEach {
                data.add(ArtistData(
                    it.artist.id,
                    it.artist.name,
                    it.albums.size
                ))
            }
            artistIndex.add(ArtistIndex(char.toString(), data))
        }
        success(context, ArtistsView(Artists(ignoredArticles, artistIndex)))
    })*/
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
                        metaartist.albums.size
                    )
                )
            } else {
                artistMap[fChar] = mutableListOf<ArtistData>(
                    ArtistData(
                        metaartist.artist.id,
                        metaartist.artist.name,
                        metaartist.albums.size
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

    var char = name
    for(art in igArt) {
        if(char.startsWith("$art ", ignoreCase = true)) {
            char = char.removePrefix("$art ")
        }
    }
    char = char.first().uppercase()

    return if (!char.first().isLetter()) {
        "#"
    } else {
        char
    }
}