package omega.sunkey.sunkensonic.server.dataclasses

data class SearchView(
    val searchResult3: Search
)

data class Search(
    val artist: List<ArtistData>,
    val album: List<AlbumData>,
    val song: List<SongData>
)