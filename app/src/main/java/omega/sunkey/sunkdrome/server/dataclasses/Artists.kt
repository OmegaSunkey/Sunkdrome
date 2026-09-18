package omega.sunkey.sunkdrome.server.dataclasses

data class Artists(
    val ignoredArticles: List<String>,
    val index: List<ArtistIndex>
)

data class ArtistIndex(
    val name: String, //this is by index: A B C .. Z #
    val artist: List<ArtistData>
)

data class ArtistData(
    val id: String,
    val name: String,
    val albumCount: Int
)

data class ArtistsView(
    val artists: Artists
)

// getArtist
data class SingleArtistView(
    val artist: SingleArtist
)

data class SingleArtist(
    val id: String,
    val name: String,
    val albumCount: Int,
    val album: List<AlbumsWithoutSongs>
)

data class AlbumsWithoutSongs(
    val id: String,
    val title: String,
    val artist: String,
    val songCount: Int,
    val duration: Int?
)