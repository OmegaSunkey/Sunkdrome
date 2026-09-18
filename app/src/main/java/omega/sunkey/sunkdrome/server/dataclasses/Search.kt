package omega.sunkey.sunkdrome.server.dataclasses

data class SearchView(
    val searchResult3: Search
)

data class Search(
    val artist: List<ArtistData>,
    val album: List<AlbumsWithoutSongs>,
    val song: List<SongData>
)