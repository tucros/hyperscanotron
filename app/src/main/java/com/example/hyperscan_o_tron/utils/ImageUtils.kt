package com.example.hyperscan_o_tron.utils

import android.content.Context
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import java.io.File

object ImageUtils {

    suspend fun compressImage(context: Context, imageFile: File) {
        Compressor.compress(context, imageFile) {
            quality(80)
            // Additional compression options can be set here
        }
    }
}