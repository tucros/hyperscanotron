package com.example.hyperscan_o_tron.data

import androidx.room.Entity

@Entity(tableName = "products", primaryKeys = ["upcCode", "scanId"])
data class Product(
    val upcCode: String,
    val scanId: Long,
    val timestamp: Long,
    var shelfTagUri: String? = null,
    var shelfTagThumbnailUri: String? = null,
    var frontImageUri: String? = null,
    var frontImageThumbnailUri: String? = null,
    var backImageUri: String? = null,
    var backImageThumbnailUri: String? = null
)