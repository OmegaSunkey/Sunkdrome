package omega.sunkey.sunkdrome.server.dataclasses

import com.fasterxml.jackson.annotation.JsonInclude

data class AlbumView(
    val album: AlbumData
)

data class AlbumData(
    val id: String,
    val parent: String,
    val album: String,
    val title: String = album,
    val name: String = album,
    val isDir: Boolean = true,
    val coverArt: String,
    val songCount: Int,
    val created: String = "2021-07-22T02:09:31+00:00",
    val duration: Int? = 0,
    val playCount: Int = 0,
    val artistId: String,
    val artist: String,
    val year: Int? = 2000,
    val genre: String = "",
    val userRating: Int = 0,
    val averageRating: Int = 0,
    val starred: String? = null,
    @param:JsonInclude(JsonInclude.Include.NON_NULL)
    val song: List<SongData>? = null,
)
