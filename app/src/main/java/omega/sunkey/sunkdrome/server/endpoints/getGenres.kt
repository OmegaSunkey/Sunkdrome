package omega.sunkey.sunkdrome.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkdrome.server.dataclasses.Genre
import omega.sunkey.sunkdrome.server.dataclasses.Genres
import omega.sunkey.sunkdrome.server.dataclasses.GenresView
import omega.sunkey.sunkdrome.server.room.SubsonicDao
import omega.sunkey.sunkdrome.server.success

fun getGenres(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    context.future(scope.future {
        val allSongs = dao.getAllSongs()
        val genreList = allSongs.groupBy { it.genre }
        val genres = mutableListOf<Genre>()

        for (genre in genreList) {
            val albumList = genre.value.groupBy { it.albumId }
            genres.add(Genre(
                genre.value.size,
                albumList.size,
                genre.key ?: "Unknown Genre"
            ))
        }
        success(context, GenresView(Genres(genres)))
    })
}