package omega.sunkey.sunkdrome.server.room

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.Index

@Entity(
    tableName = "songs",
    foreignKeys = [ForeignKey(
        entity = Album::class,
        parentColumns = ["id"],
        childColumns = ["album_id"],
        onDelete = CASCADE
    )],
    indices = [Index("album_id")]
)
data class Song(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "track") val track: Int? = null,
    @ColumnInfo(name = "year") val year: Int? = null,
    @ColumnInfo(name = "genre") val genre: String? = null,
    @ColumnInfo(name = "size") val size: Long = 0,
    @ColumnInfo(name = "suffix") val suffix: String? = "",
    @ColumnInfo(name = "duration") val duration: Int,
    @ColumnInfo(name = "date_added") val dateAdded: Long = 0,
    @ColumnInfo(name = "path") val path: String,
    @ColumnInfo(name = "album_id") val albumId: String,
    @ColumnInfo(name = "artist_id") val artistId: String,
)

@Entity(
    tableName = "albums",
    foreignKeys = [ForeignKey(
        entity = Artist::class,
        parentColumns = ["id"],
        childColumns = ["artist_id"],
        onDelete = CASCADE
    )],
    indices = [Index("artist_id")]
)
data class Album(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "artist_id") val artistId: String,
    @ColumnInfo(name = "year") val year: Int? = null,
)

@Entity(
    tableName = "artists"
)
data class Artist(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
)

data class AlbumsWithMeta(
    @Embedded val album: Album,

    @Relation(
        parentColumn = "id",
        entityColumn = "album_id"
    )
    val songs: List<Song>,

    @Relation(
        parentColumn = "artist_id",
        entityColumn = "id"
    )
    val artist: Artist
)

data class ArtistsWithMeta(
    @Embedded val artist: Artist,

    @Relation(
        entity = Album::class,
        parentColumn = "id",
        entityColumn = "artist_id"
    )
    val albums: List<AlbumsWithMeta>
)

data class SongWithMetadata(
    @Embedded val song: Song,

    @Relation(
        parentColumn = "album_id",
        entityColumn = "id"
    )
    val album: Album,
    @Relation(
        parentColumn = "artist_id",
        entityColumn = "id"
    )
    val artist: Artist
)