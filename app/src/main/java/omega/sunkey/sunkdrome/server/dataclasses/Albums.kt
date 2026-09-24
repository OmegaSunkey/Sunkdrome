package omega.sunkey.sunkdrome.server.dataclasses

data class AlbumView(
    val album: AlbumData
)

data class AlbumData(
    val id: String,
    val title: String,
    val artist: String,
    val songCount: Int,
    val duration: Int?,
    val coverArt: String
    val song: List<SongData>,
)
