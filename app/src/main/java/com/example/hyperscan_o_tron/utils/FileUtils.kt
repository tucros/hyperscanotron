package com.example.hyperscan_o_tron.utils

import android.content.Context
import java.io.File

object FileUtils {

    fun createFile(context: Context, upcCode: String, fileName: String): File {
        val mediaDir = File(
            context.getExternalFilesDir(null),
            "Products/$upcCode"
        )

        if (!mediaDir.exists()) {
            mediaDir.mkdirs()
        }

        return File(mediaDir, fileName)
    }

    fun getFilePath(context: Context, upcCode: String, fileName: String): String? {
        val file = File(
            context.getExternalFilesDir(null),
            "Products/$upcCode/$fileName"
        )
        return if (file.exists()) file.absolutePath else null
    }
}