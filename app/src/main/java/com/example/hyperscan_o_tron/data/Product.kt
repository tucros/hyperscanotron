package com.example.hyperscan_o_tron.data

import androidx.room.Entity

@Entity(tableName = "products", primaryKeys = ["upcCode", "scanId"])
data class Product(
    val upcCode: String,
    val scanId: Long,
    val timestamp: Long,
    var shelfTagUri: String?,
    var frontImageUri: String?,
    var backImageUri: String?
)