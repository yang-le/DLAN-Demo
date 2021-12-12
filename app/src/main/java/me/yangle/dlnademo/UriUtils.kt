package me.yangle.dlnademo

import java.io.InputStream
import java.io.OutputStream

/*
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28" />
*/

/*
fun queryPath(
    contentResolver: ContentResolver,
    uri: Uri,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    projection: String = MediaStore.MediaColumns.DATA,
): String? {
    Log.i("UriUtils", "query uri is $uri")
    contentResolver.query(
        uri,
        arrayOf(projection),
        selection,
        selectionArgs,
        null
    )?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex(MediaStore.MediaColumns.DATA)
            if (index >= 0) {
                return it.getString(index)
            }
        }
    }
    return null
}

fun uriToPath(context: Context, uri: Uri): String? {
    Log.i("UriUtils", "input uri is $uri")
    var path:String? = null
    when {
        DocumentsContract.isDocumentUri(context, uri) -> {
            when (uri.authority) {
                "com.android.externalstorage.documents" -> {
                    val split = DocumentsContract.getDocumentId(uri).split(":")
                    if ("primary".equals(split[0], true)) {
                        path =  "${Environment.getExternalStorageDirectory().absolutePath}/${split[1]}"
                    }
                }
                "com.android.providers.media.documents" -> {
                    val split = DocumentsContract.getDocumentId(uri).split(":")
                    val contentUri = when (split[0]) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> null
                    }
                    contentUri?.let {
                        path = queryPath(
                            context.contentResolver,
                            it,
                            "${MediaStore.MediaColumns._ID} = ?",
                            arrayOf(split[1])
                        )
                    }
                }
            }
        }
        "content".equals(uri.scheme, true) -> {
            path = queryPath(context.contentResolver, uri)
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
        path = file.absolutePath
        Log.i("UriUtils", "fallback to $path")
    }

    return path
}
*/

fun copyFile(inFile: InputStream, outFile: OutputStream) =
    inFile.use { input ->
        outFile.use { output ->
            input.copyTo(output)
        }
    }
