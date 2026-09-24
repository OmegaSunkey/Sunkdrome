package omega.sunkey.sunkdrome.server.dataclasses

data class Artists(
    val ignoredArticles: String,
    val index: List<ArtistIndex>
)

data class ArtistIndex(
    val name: String, //this is by index: A B C .. Z #
    val artist: List<ArtistData>
)

data class ArtistData(
    val id: String,
    val name: String,
    val albumCount: Int,
    val coverArt: String
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
    val coverArt: String,
    val albumCount: Int,
    val userRating: Int = 0,
    val artistImageUrl: String = "",
    val starred: String = "",
    val musicBrainzId: String = "",
    val sortName: String = "",
    val roles: List<String> = listOf(""),
    val album: List<AlbumsWithoutSongs>
)

data class AlbumsWithoutSongs(
    val id: String,
    val title: String,
    val artist: String,
    val songCount: Int,
    val duration: Int?,
    val coverArt: String
)