package com.example.hyperscan_o_tron.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore

object FileUtils {

    fun createImageFile(
        context: Context,
        scanId: Long,
        fileName: String
    ): Uri? {
        val imageCollectionUri: Uri =
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/HyperScanOTron/$scanId"
            )
        }

        val imageUri = context.contentResolver.insert(imageCollectionUri, imageDetails)
        return imageUri
    }
}
