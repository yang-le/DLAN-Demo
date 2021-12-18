package me.yangle.dlnademo.upnp

import android.database.Cursor
import android.provider.MediaStore.MediaColumns.*
import me.yangle.dlnademo.getIntColumn
import me.yangle.dlnademo.getStringColumn
import org.cybergarage.upnp.std.av.server.`object`.DIDLLite
import org.cybergarage.upnp.std.av.server.`object`.item.ItemNode
import org.cybergarage.upnp.std.av.server.`object`.item.ResourceNode
import org.cybergarage.xml.Attribute
import org.cybergarage.xml.AttributeList
import java.time.Instant

object DIDLLiteHelper {
    const val MOVIE = "object.item.movie"
    const val VIDEO = "object.item.video"
    const val AUDIO = "object.item.audio"
    const val MUSIC = "object.item.music"
    const val IMAGE = "object.item.image"
    const val PHOTO = "object.item.photo"

    private fun findStringColunm(
        cursor: Cursor?,
        columns: List<String>,
        default: String = ItemNode.UNKNOWN
    ) =
        columns.map { getStringColumn(cursor, it) }.find { it != null && it != "" }
            ?: default

    private fun findIntColunm(cursor: Cursor?, columns: List<String>, default: Int = 0) =
        columns.map { getIntColumn(cursor, it) }.find { it != null } ?: default

    private fun getId(cursor: Cursor?) = findStringColunm(
        cursor, listOf(
            DOCUMENT_ID,
            ORIGINAL_DOCUMENT_ID,
            INSTANCE_ID,
            CD_TRACK_NUMBER
        ), "0"
    )

    private fun getTile(cursor: Cursor?) = findStringColunm(
        cursor, listOf(TITLE)
    )

    private fun getCreator(cursor: Cursor?) = findStringColunm(
        cursor, listOf(
            ARTIST,
            ALBUM_ARTIST,
            COMPOSER,
            AUTHOR,
            WRITER,
            COMPILATION
        )
    )

    private fun getDate(cursor: Cursor?) = findIntColunm(
        cursor, listOf(
            YEAR,
            DATE_TAKEN,
            DATE_ADDED,
            DATE_MODIFIED,
        )
    )

    private fun getMime(cursor: Cursor?) = findStringColunm(
        cursor, listOf(MIME_TYPE)
    )

    private fun getResolution(cursor: Cursor?) = findStringColunm(
        cursor, listOf(RESOLUTION)
    )

    private fun getDuration(cursor: Cursor?) = findIntColunm(
        cursor, listOf(DURATION)
    )

    private fun itemNode(
        id: String,
        parentID: String,
        restricted: Int,
        title: String,
        creator: String,
        uPnPClass: String,
        date: Long,
        url: String,
        protocolInfo: String,
        resolution: String,
        duration: Int
    ) = ItemNode().apply {
        setID(id)
        setParentID(parentID)
        setRestricted(restricted)
        setTitle(title)
        setCreator(creator)
        setUPnPClass(uPnPClass)
        setDate(date)
        setResource(url, protocolInfo, AttributeList().apply {
            add(Attribute(ResourceNode.RESOLUTION, resolution))
            add(Attribute("duration", duration.toString()))
        })
    }

    private fun metaData(
        itemNode: ItemNode
    ) = DIDLLite().apply {
        addContentNode(itemNode)
    }.toString()

    fun metaData(
        id: String = "0",
        parentID: String = "0",
        restricted: Int = 1,
        title: String = ItemNode.UNKNOWN,
        creator: String = ItemNode.UNKNOWN,
        uPnPClass: String = ItemNode.UNKNOWN,
        date: Long = Instant.now().toEpochMilli(),
        url: String = ItemNode.UNKNOWN,
        protocolInfo: String = ItemNode.UNKNOWN,
        resolution: String = ItemNode.UNKNOWN,
        duration: Int
    ) = metaData(
        itemNode(
            id,
            parentID,
            restricted,
            title,
            creator,
            uPnPClass,
            date,
            url,
            protocolInfo,
            resolution,
            duration
        )
    )

    fun metaData(
        id: String = "0",
        parentID: String = "0",
        restricted: Boolean,
        title: String = ItemNode.UNKNOWN,
        creator: String = ItemNode.UNKNOWN,
        uPnPClass: String = ItemNode.UNKNOWN,
        date: Long = Instant.now().toEpochMilli(),
        url: String = ItemNode.UNKNOWN,
        protocolInfo: String = ItemNode.UNKNOWN,
        resolution: String = "",
        duration: Int
    ) = metaData(
        id,
        parentID,
        if (restricted) 1 else 0,
        title,
        creator,
        uPnPClass,
        date,
        url,
        protocolInfo,
        resolution,
        duration
    )

    fun metaData(
        id: String = "0",
        parentID: String = "0",
        restricted: Int = 1,
        title: String = ItemNode.UNKNOWN,
        creator: String = ItemNode.UNKNOWN,
        uPnPClass: String = ItemNode.UNKNOWN,
        date: String,
        url: String = ItemNode.UNKNOWN,
        protocolInfo: String = ItemNode.UNKNOWN,
        resolution: String = "",
        duration: Int
    ) = metaData(
        itemNode(
            id,
            parentID,
            restricted,
            title,
            creator,
            uPnPClass,
            0,
            url,
            protocolInfo,
            resolution,
            duration
        ).apply { setDate(date) }
    )

    fun metaData(cursor: Cursor?) = metaData(
        id = getId(cursor),
        title = getTile(cursor),
        creator = getCreator(cursor),
        date = getDate(cursor).toLong(),
        resolution = getResolution(cursor),
        duration = getDuration(cursor)
    )
}