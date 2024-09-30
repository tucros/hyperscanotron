package com.example.hyperscan_o_tron.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val upcCode: String,
    val scanId: Long,
    val timestamp: Long,
    var shelfTagUri: String?,
    var frontImageUri: String?,
    var backImageUri: String?
)