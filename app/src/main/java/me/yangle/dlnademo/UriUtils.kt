package me.yangle.dlnademo

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import java.io.File
import java.io.InputStream
import java.io.OutputStream

private const val TAG = "UriUtils"

fun getStringColumn(cursor: Cursor?, column: String): String? {
    cursor?.apply {
        if (moveToFirst()) {
            val index = getColumnIndex(column)
            if (index >= 0) {
                val result = getStringOrNull(index)
                Log.i(TAG, "$column : $result")
                return result
            }
        }
    }
    return null
}

fun getIntColumn(cursor: Cursor?, column: String): Int? {
    cursor?.apply {
        if (moveToFirst()) {
            val index = getColumnIndex(column)
            if (index >= 0) {
                val result = getIntOrNull(index)
                Log.i(TAG, "$column : $result")
                return result
            }
        }
    }
    return null
}

fun getCursor(context: Context, uri: Uri): Cursor? {
    Log.i(TAG, "input uri is $uri")
    when {
        uri.authority == "com.android.providers.media.documents" -> {
            val split = DocumentsContract.getDocumentId(uri).split(":")
            val contentUri = when (split[0]) {
                "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> null
            }
            Log.i(TAG, "query uri is $contentUri")
            contentUri?.let {
                return context.contentResolver.query(
                    it,
                    null,
                    "${MediaStore.MediaColumns._ID} = ?",
                    arrayOf(split[1]),
                    null
                )
            }
        }
        "content".equals(uri.scheme, true) -> {
            Log.i(TAG, "query uri is $uri")
            return context.contentResolver.query(
                uri,
                null,
                null,
                null
            )
        }
    }
    return null
}

fun getPath(context: Context, uri: Uri): String? {
    var path: String? = getStringColumn(getCursor(context, uri), MediaStore.MediaColumns.DATA)
    if (path != null && path != "") return path

    when {
        uri.authority == "com.android.externalstorage.documents" -> {
            val split = DocumentsContract.getDocumentId(uri).split(":")
            if ("primary".equals(split[0], true)) {
                path =
                    "${Environment.getExternalStorageDirectory().absolutePath}/${split[1]}"
            }
        }
        "file".equals(uri.scheme, true) -> {
            path = uri.path
        }
    }

    if (path == null || path == "") {
        val file = File(context.cacheDir, "uri_utils_temp")
        context.contentResolver.openInputStream(uri)?.let {
            copyFile(it, file.outputStream())
        }
        path = file.path
        Log.i(TAG, "fallback to $path")
    }

    return path
}

fun copyFile(inFile: InputStream, outFile: OutputStream) =
    inFile.use { input ->
        outFile.use { output ->
            input.copyTo(output)
        }
    }
