package omega.sunkey.sunkdrome.server.room

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction

@Database(entities = [Artist::class, Album::class, Song::class], version = 1)
abstract class SubsonicDatabase : RoomDatabase() {
    abstract fun subsonicDao(): SubsonicDao
    companion object {
        @Volatile
        private var INSTANCE: SubsonicDatabase? = null
        fun getInstance(context: Context): SubsonicDatabase {
            return INSTANCE?: synchronized(this) {
                val inst = Room.databaseBuilder(
                    context.applicationContext,
                    SubsonicDatabase::class.java,
                    "subsonic_database"
                )
                    .fallbackToDestructiveMigration() //DONT FORGET !!!!!!!!!
                    .build()
                INSTANCE = inst
                inst
            }
        }
    }
}

@Dao
interface SubsonicDao {
    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<Song>

    @Transaction
    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongById(songId: String): SongWithMetadata?

    @Query("SELECT path FROM songs")
    suspend fun getAllSongPaths(): List<String>
    @Transaction
    @Query("SELECT * FROM songs WHERE path LIKE :path || '%' ORDER BY path ASC")
    suspend fun getSongsByPath(path: String): List<SongWithMetadata>

    @Transaction
    @Query("SELECT * FROM albums")
    suspend fun getAllALbumsWithMeta(): List<AlbumsWithMeta>

    @Query("SELECT * FROM albums")
    suspend fun getAllAlbums(): List<Album>

    @Transaction
    @Query("SELECT * FROM albums WHERE id = :albumId")
    suspend fun getAlbumWithSongs(albumId: String): AlbumsWithMeta?

    @Query("SELECT * FROM artists ORDER BY name ASC")
    suspend fun getAllArtists(): List<Artist>

    @Transaction
    @Query("SELECT * FROM artists ORDER BY name ASC")
    suspend fun getAllArtistsWithMeta(): List<ArtistsWithMeta>

    @Transaction
    @Query("SELECT * FROM artists WHERE id = :artistId")
    suspend fun getArtistWithDetails(artistId: String): ArtistsWithMeta?

    @Query("SELECT * FROM artists WHERE id = :artistId")
    suspend fun getArtist(artistId: String): Artist?

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' LIMIT 20")
    suspend fun searchSongs(query: String): List<Song>

    @Query("SELECT * FROM albums WHERE title LIKE '%' || :query || '%' LIMIT 20")
    suspend fun searchAlbums(query: String): List<Album>

    @Query("SELECT * FROM artists WHERE name LIKE '%' || :query || '%' LIMIT 20")
    suspend fun searchArtists(query: String): List<Artist>

    @Insert(onConflict = REPLACE)
    suspend fun insertArtists(artists: List<Artist>)

    @Insert(onConflict = REPLACE)
    suspend fun insertAlbums(albums: List<Album>)

    @Insert(onConflict = REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    @Query("DELETE FROM songs")
    suspend fun clearAllSongs()

    @Query("DELETE FROM albums")
    suspend fun clearAllAlbums()

    @Query("DELETE FROM artists")
    suspend fun clearAllArtists()
}