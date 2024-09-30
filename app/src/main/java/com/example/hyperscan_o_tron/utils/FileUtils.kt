package com.example.hyperscan_o_tron.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File

object FileUtils {
    fun createScanFolder(context: Context, scanId: Long): File? {
        val rootDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null
        val scanFolder = File(rootDir, "Scan_$scanId")

        return if (scanFolder.exists() || scanFolder.mkdirs()) {
            scanFolder
        } else {
            Log.e("FileUtils", "Failed to create scan folder: $scanFolder")
            null
        }
    }

    /**
     * Creates a file in the user-selected storage location or default app-specific storage.
     */
    fun createImageFile(
        context: Context,
        scanFolder: File,
        upcCode: String,
        fileName: String
    ): Uri? {
        val newFileName = "${upcCode}_$fileName"
        val file = File(scanFolder, newFileName)
        return Uri.fromFile(file)
    }
}
