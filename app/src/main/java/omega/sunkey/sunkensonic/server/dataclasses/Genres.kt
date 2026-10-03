package omega.sunkey.sunkensonic.server.dataclasses


data class Genre(
    var songCount: Int,
    var albumCount: Int,
    val value: String,
)
data class Genres(
    val genre: List<Genre>
)

data class GenresView(
    val genres: Genres
)
