package com.example.hyperscan_o_tron.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import java.io.File

object FileUtils {

    private const val PREFS_NAME = "app_prefs"
    private const val KEY_STORAGE_URI = "storage_uri"

    /**
     * Creates a file in the user-selected storage location or default app-specific storage.
     */
    fun createFile(context: Context, upcCode: String, fileName: String): Uri? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uriString = prefs.getString(KEY_STORAGE_URI, null)

        val newFileName = "$upcCode/$fileName"
        return if (uriString != null) {
            // User has selected a storage location
            val folderUri = Uri.parse(uriString)
            val folderDocumentFile = DocumentFile.fromTreeUri(context, folderUri)
            if (folderDocumentFile != null && folderDocumentFile.canWrite()) {
                val mimeType = "image/jpeg"
                val newFile = folderDocumentFile.createFile(mimeType, newFileName)
                newFile?.uri
            } else {
                null
            }
        } else {
            // User has not selected a storage location, use app-specific storage
            val appSpecificDir = getAppSpecificStorageDir(context, upcCode)
            if (!appSpecificDir.exists()) {
                appSpecificDir.mkdirs()
            }
            val file = File(appSpecificDir, newFileName)
            Uri.fromFile(file)
        }
    }

    /**
     * Retrieves the file path or URI for a given file.
     */
    fun getFilePath(context: Context, upcCode: String, fileName: String): Uri? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uriString = prefs.getString(KEY_STORAGE_URI, null)

        return if (uriString != null) {
            val folderUri = Uri.parse(uriString)
            val folderDocumentFile = DocumentFile.fromTreeUri(context, folderUri)
            if (folderDocumentFile != null && folderDocumentFile.canRead()) {
                val productDir = folderDocumentFile.findFile(upcCode)
                val fileDocument = productDir?.findFile(fileName)
                fileDocument?.uri
            } else {
                null
            }
        } else {
            val appSpecificDir = getAppSpecificStorageDir(context, upcCode)
            val file = File(appSpecificDir, fileName)
            if (file.exists()) {
                Uri.fromFile(file)
            } else {
                null
            }
        }
    }

    /**
     * Returns the app-specific storage directory for a given UPC code.
     */
    private fun getAppSpecificStorageDir(context: Context, upcCode: String): File {
        return File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "Products/$upcCode"
        )
    }
}
