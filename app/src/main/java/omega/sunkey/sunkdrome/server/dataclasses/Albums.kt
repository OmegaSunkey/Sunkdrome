package omega.sunkey.sunkdrome.server.dataclasses

import omega.sunkey.sunkdrome.server.room.Song

data class AlbumView(
    val album: AlbumData
)

data class AlbumData(
    val id: String,
    val title: String,
    val artist: String,
    val songCount: Int,
    val duration: Int?,
    val song: List<Song>,
    val coverArt: String
)