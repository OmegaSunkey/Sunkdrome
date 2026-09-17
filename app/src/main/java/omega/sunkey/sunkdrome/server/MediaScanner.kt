package omega.sunkey.sunkdrome.server

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import omega.sunkey.sunkdrome.server.room.Artist
import omega.sunkey.sunkdrome.server.room.Song
import omega.sunkey.sunkdrome.server.room.Album
import omega.sunkey.sunkdrome.server.room.SubsonicDatabase
import java.security.MessageDigest

class MediaScanner (
    context: Context,
    p: WorkerParameters
) : CoroutineWorker(context, p) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val db = SubsonicDatabase.getInstance(applicationContext)
                val dao = db.subsonicDao()
                Log.i("MediaScanner", "scanning !!!!!!!!!!!!!!")
                dao.clearAllSongs()
                dao.clearAllAlbums()
                dao.clearAllArtists()
                val audioFiles = queryAudioFiles()
                Log.i("MediaScanner", "found ${audioFiles.size} !!!! much!")

                val artists = mutableMapOf<String, Artist>()
                val albums = mutableMapOf<String, Album>()
                val songs = mutableListOf<Song>()
                for (file in audioFiles) {
                    val meta = extractMeta(file.uri)
                    val name = meta.artist ?: "Unknown Artist"
                    val artid = name.md5()
                    if(!artists.containsKey(artid)) {
                        artists[artid] = Artist(artid, name)
                    }

                    val album = meta.album ?: "Unknown Album"
                    val aid = "${artid}_${album}".md5()
                    if (!albums.containsKey(aid)) {
                        albums[aid] = Album(aid, album, artid, meta.year)
                    }

                    val song = Song(
                        id = file.id.toString(),
                        title = meta.title ?: file.displayName,
                        albumId = aid,
                        artistId = artid,
                        duration = file.duration / 1000,
                        path = file.path,
                        size = file.size,
                        track = meta.track,
                        year = meta.year,
                        genre = meta.genre,
                        suffix = file.displayName.substringAfterLast('.', ""),
                        dateAdded = file.dateAdded
                    )
                    songs.add(song)
                }
                dao.insertArtists(artists.values.toList())
                dao.insertAlbums(albums.values.toList())
                dao.insertSongs(songs)
                Log.i("MediaScanner", "done :D | indexed ${artists.size} artists, ${albums.size} albums, and ${songs.size} songs !!!!")
                Result.success()
            } catch (e: Exception) {
                Log.e("MediaScanner", "oops", e)
                Result.retry()
            }
        }
    }
    private fun queryAudioFiles(): List<AudioFile> {
        val audioFiles = mutableListOf<AudioFile>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED
        )
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        applicationContext.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val name = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dur = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val siz = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val data = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val date = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val idd = cursor.getLong(id)
                val namee = cursor.getString(name)
                val dura = cursor.getInt(dur)
                val size = cursor.getLong(siz)
                val path = cursor.getString(data)
                val dateAdded = cursor.getLong(date)
                val cUri = ContentUris.withAppendedId(collection, idd)
                audioFiles.add(AudioFile(idd, namee, dura, size, path, dateAdded, cUri))
            }
        }
        return audioFiles
    }
    private fun extractMeta(uri: Uri): AudioMetadata {
        val retriever = android.media.MediaMetadataRetriever()
        return try {
            retriever.setDataSource(applicationContext, uri)
            AudioMetadata(
                title = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE),
                artist = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST),
                album = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ALBUM),
                track = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.toIntOrNull(),
                year = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull(),
                genre = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_GENRE)
            )
        } catch (e: Exception) {
            Log.w("MediaScanner", "Failed to extract metadata from $uri", e)
            AudioMetadata()
        } finally {
            retriever.release()
        }
    }

    fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(this.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private data class AudioFile(
        val id: Long,
        val displayName: String,
        val duration: Int,
        val size: Long,
        val path: String,
        val dateAdded: Long,
        val uri: Uri
    )
    private data class AudioMetadata(
        val title: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val track: Int? = null,
        val year: Int? = null,
        val genre: String? = null
    )
}