package me.yangle.dlnademo.upnp

import android.database.Cursor
import android.provider.MediaStore.Audio.AudioColumns
import android.provider.MediaStore.Images.ImageColumns
import android.provider.MediaStore.MediaColumns
import android.provider.MediaStore.Video.VideoColumns
import me.yangle.dlnademo.getIntColumn
import me.yangle.dlnademo.getStringColumn
import org.fourthline.cling.support.contentdirectory.DIDLParser
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.DIDLObject
import org.fourthline.cling.support.model.Res
import org.fourthline.cling.support.model.item.*
import java.time.Instant

object DIDLLiteHelper {
    private fun findStringColumn(
        cursor: Cursor?,
        columns: List<String>,
        default: String = ""
    ) =
        columns.map { getStringColumn(cursor, it) }.find { it != null && it != "" }
            ?: default

    private fun findIntColumn(cursor: Cursor?, columns: List<String>, default: Int = 0) =
        columns.map { getIntColumn(cursor, it) }.find { it != null } ?: default

    private fun hasColumn(cursor: Cursor?, columns: List<String>) =
        columns.map { cursor?.getColumnIndex(it) }.find { it != null && it != -1 } != null

    private fun getId(cursor: Cursor?) = findStringColumn(
        cursor, listOf(
            MediaColumns.DOCUMENT_ID,
            MediaColumns.ORIGINAL_DOCUMENT_ID,
            MediaColumns.INSTANCE_ID,
            MediaColumns.CD_TRACK_NUMBER
        ), "0"
    )

    private fun getTile(cursor: Cursor?) = findStringColumn(
        cursor, listOf(MediaColumns.TITLE)
    )

    private fun getAlbum(cursor: Cursor?) = findStringColumn(
        cursor, listOf(MediaColumns.ALBUM)
    )

    private fun getAlbumArtist(cursor: Cursor?) = findStringColumn(
        cursor, listOf(
            MediaColumns.ARTIST,
            MediaColumns.ALBUM_ARTIST
        )
    )

    private fun getCreator(cursor: Cursor?) = findStringColumn(
        cursor, listOf(
            MediaColumns.AUTHOR,
            MediaColumns.WRITER,
            MediaColumns.COMPILATION,
            MediaColumns.ARTIST,
            MediaColumns.ALBUM_ARTIST,
            MediaColumns.COMPOSER
        )
    )

    private fun getDate(cursor: Cursor?) = findIntColumn(
        cursor, listOf(
            MediaColumns.DATE_TAKEN,
            MediaColumns.DATE_ADDED,
            MediaColumns.DATE_MODIFIED,
        )
    )

//    private fun getYear(cursor: Cursor?) = findStringColumn(
//        cursor, listOf(MediaColumns.YEAR)
//    )

    private fun getMime(cursor: Cursor?) = findStringColumn(
        cursor, listOf(MediaColumns.MIME_TYPE)
    )

//    private fun getResolution(cursor: Cursor?) = findStringColumn(
//        cursor, listOf(MediaColumns.RESOLUTION)
//    )

    private fun getDuration(cursor: Cursor?) = findIntColumn(
        cursor, listOf(MediaColumns.DURATION)
    )

    private fun getBitrate(cursor: Cursor?) = findIntColumn(
        cursor, listOf(MediaColumns.BITRATE)
    )

    private fun isAudio(cursor: Cursor?) = hasColumn(
        cursor, listOf(
            AudioColumns.IS_ALARM,
            AudioColumns.IS_AUDIOBOOK,
            AudioColumns.IS_MUSIC,
            AudioColumns.IS_NOTIFICATION,
            AudioColumns.IS_PODCAST,
            AudioColumns.IS_RINGTONE
        )
    )

    private fun isMusic(cursor: Cursor?) = findIntColumn(
        cursor, listOf(AudioColumns.IS_MUSIC)
    ) == 1

    private fun isPodcast(cursor: Cursor?) = findIntColumn(
        cursor, listOf(AudioColumns.IS_PODCAST)
    ) == 1

    private fun isAudioBook(cursor: Cursor?) = findIntColumn(
        cursor, listOf(AudioColumns.IS_AUDIOBOOK)
    ) == 1

    private fun isImage(cursor: Cursor?) = hasColumn(
        cursor, listOf(ImageColumns.SCENE_CAPTURE_TYPE)
    )

    private fun isPhoto(cursor: Cursor?) = findStringColumn(
        cursor, listOf(
            ImageColumns.EXPOSURE_TIME,
            ImageColumns.F_NUMBER
        )
    ).isNotEmpty() || findIntColumn(
        cursor, listOf(ImageColumns.ISO)
    ) != 0

    private fun isVideo(cursor: Cursor?) = hasColumn(
        cursor, listOf(
            VideoColumns.COLOR_RANGE,
            VideoColumns.COLOR_STANDARD,
            VideoColumns.COLOR_TRANSFER
        )
    )

    private fun isMovie(cursor: Cursor?) = findStringColumn(
        cursor, listOf(VideoColumns.LANGUAGE)
    ).isNotEmpty()

//    private fun isMusicVideo(cursor: Cursor?) = findStringColumn(
//        cursor, listOf(MediaColumns.ALBUM)
//    ).isNotEmpty() && isVideo(cursor)

    private fun isYoutube(cursor: Cursor?) = findStringColumn(
        cursor, listOf(VideoColumns.CATEGORY)
    ).isNotEmpty()

    fun getMetadata(cursor: Cursor?, url: String): String =
        DIDLParser().generate(
            DIDLContent().addObject(
                when {
                    isYoutube(cursor) -> {
                        VideoBroadcast(getId(cursor), "0", getTile(cursor), getCreator(cursor))
                    }
//            isMusicVideo(cursor) -> {
//                MusicVideoClip(getId(cursor), "0", getTile(cursor), getCreator(cursor))
//            }
                    isMovie(cursor) -> {
                        Movie(getId(cursor), "0", getTile(cursor), getCreator(cursor))
                    }
                    isVideo(cursor) -> {
                        VideoItem(getId(cursor), "0", getTile(cursor), getCreator(cursor))
                    }
                    isPhoto(cursor) -> {
                        Photo(
                            getId(cursor),
                            "0",
                            getTile(cursor),
                            getCreator(cursor),
                            getAlbum(cursor)
                        )
                    }
                    isImage(cursor) -> {
                        ImageItem(getId(cursor), "0", getTile(cursor), getCreator(cursor))
                    }
                    isAudioBook(cursor) -> {
                        AudioBook(
                            getId(cursor),
                            "0",
                            getTile(cursor),
                            getCreator(cursor),
                            null,
                            null,
                            Instant.ofEpochMilli(getDate(cursor).toLong()).toString()
                        )
                    }
                    isPodcast(cursor) -> {
                        AudioBroadcast(getId(cursor), "0", getTile(cursor), getCreator(cursor))
                    }
                    isMusic(cursor) -> {
                        MusicTrack(
                            getId(cursor),
                            "0",
                            getTile(cursor),
                            getCreator(cursor),
                            getAlbum(cursor),
                            getAlbumArtist(cursor)
                        )
                    }
                    isAudio(cursor) -> {
                        AudioItem(getId(cursor), "0", getTile(cursor), getCreator(cursor))
                    }
                    else -> {
                        Item(
                            getId(cursor),
                            "0",
                            getTile(cursor),
                            getCreator(cursor),
                            DIDLObject.Class("object.item")
                        )
                    }
                }.addResource(
                    Res(
                        getMime(cursor),
                        0,
                        getDuration(cursor).toString(),
                        getBitrate(cursor).toLong(),
                        url
                    )
                )
            )
        )
}