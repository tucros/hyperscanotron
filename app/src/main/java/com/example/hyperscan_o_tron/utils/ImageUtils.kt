package com.example.hyperscan_o_tron.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

object ImageUtils {

    suspend fun compressImage(context: Context, imageUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                val outputStream: OutputStream? = context.contentResolver.openOutputStream(imageUri)
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                }
                outputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}