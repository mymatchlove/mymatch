package mymatch.love.utility

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import mymatch.love.application.MyApplication
import java.io.*

/**
 * Created by Momin Nasirali on 15/11/21.
 */
class PickDocument {

    fun tryOpenDocument(requestCode: Int, data: Intent?) : File? {
        when (val openFileResult = handleOpenDocumentResult(requestCode, data)) {
            is OpenFileResult.FileWasOpened -> {
//                viewModel.uploadDocument(openFileResult.fileName, openFileResult.content)
                AppDebugLog.print("FileWasOpened fileName : ${openFileResult.fileName + " content :" + openFileResult.content}")
                return copyFileToInternal(data?.data!!,openFileResult.content)
            }
            OpenFileResult.ErrorOpeningFile -> {
                //viewModel.errorOpeningDocument()
                AppDebugLog.print("error in ErrorOpeningFile")
                return null;
            }
            OpenFileResult.OpenFileWasCancelled -> {
//                viewModel.userCancelledOpenOfDocument()
                AppDebugLog.print("error in OpenFileWasCancelled")
                return null
            }
            OpenFileResult.DifferentResult -> {
                AppDebugLog.print("do nothing")
                return null;
            }
        }
    }

    private fun copyFileToInternal(fileuri: Uri, inputStream: InputStream): File? {
        val theCursor: Cursor? = MyApplication.getContext().contentResolver.query(
            fileuri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null, null, null
        )
        theCursor?.moveToFirst()
        val displayName = theCursor?.getString(theCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        //val size = theCursor?.getLong(theCursor.getColumnIndex(OpenableColumns.SIZE))
        val theFile: File = File(MyApplication.getContext().filesDir.toString() + "/" + displayName)
        AppDebugLog.print("theFile : "+theFile.absolutePath)
        try {
            val fileOutputStream = FileOutputStream(theFile)
            val buffers = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffers).also { read = it } != -1) {
                AppDebugLog.print("read : "+read)
                fileOutputStream.write(buffers,0,read)
            }
            AppDebugLog.print("theFile : "+theFile.absolutePath.length)
            return theFile;
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun handleOpenDocumentResult(resultCode: Int, data: Intent?): OpenFileResult {
        return if (resultCode == Activity.RESULT_OK && data != null) {
            val contentUri = data.data
            if (contentUri != null) {
                val stream =
                    try {
                        MyApplication.getContext().contentResolver.openInputStream(contentUri)
                    } catch (exception: FileNotFoundException) {
                        AppDebugLog.print("Exception : " + exception)
                        return OpenFileResult.ErrorOpeningFile
                    }

                val fileName = MyApplication.getContext().contentResolver.queryFileName(contentUri)

                if (stream != null && fileName != null) {
                    OpenFileResult.FileWasOpened(fileName, stream)
                } else OpenFileResult.ErrorOpeningFile
            } else {
                OpenFileResult.ErrorOpeningFile
            }
        } else {
            OpenFileResult.OpenFileWasCancelled
        }
    }

    sealed class OpenFileResult {
        object OpenFileWasCancelled : OpenFileResult()
        data class FileWasOpened(val fileName: String, val content: InputStream) : OpenFileResult()
        object ErrorOpeningFile : OpenFileResult()
        object DifferentResult : OpenFileResult()
    }

    val allSupportedDocumentsTypesToExtensions = mapOf(
//        "application/msword" to ".doc",
//        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" to ".docx",
        "application/pdf" to ".pdf",
//        "text/rtf" to ".rtf",
//        "application/rtf" to ".rtf",
//        "application/x-rtf" to ".rtf",
//        "text/richtext" to ".rtf",
//        "text/plain" to ".txt"
    )

    private val extensionsToTypes = allSupportedDocumentsTypesToExtensions.invert()

    fun ContentResolver.queryFileName(uri: Uri): String? {
        val cursor: Cursor = query(uri, null, null, null, null) ?: return null
        val nameIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        val name: String = cursor.getString(nameIndex)
        cursor.close()
        return appendExtensionIfNeeded(name, uri)
    }

    private fun ContentResolver.appendExtensionIfNeeded(name: String, uri: Uri): String? {
        return if (hasKnownExtension(name)) {
            name
        } else {
            val type = getType(uri)
            if (type != null && allSupportedDocumentsTypesToExtensions.containsKey(type)) {
                return name + allSupportedDocumentsTypesToExtensions[type]
            }
            AppDebugLog.printError("unknown file type: $type, for file: $name")
            name
        }
    }

    private fun hasKnownExtension(filename: String): Boolean {
        val lastDotPosition = filename.indexOfLast { it == '.' }
        if (lastDotPosition == -1) {
            return false
        }
        val extension = filename.substring(lastDotPosition)
        return extensionsToTypes.containsKey(extension)
    }

    // utils function
    fun <K, V> Map<K, V>.invert(): Map<V, K> {
        val inverted = mutableMapOf<V, K>()
        for (item in this) {
            inverted[item.value] = item.key
        }
        return inverted
    }
}