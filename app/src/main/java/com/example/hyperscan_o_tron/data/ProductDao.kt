package com.example.hyperscan_o_tron.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Query("SELECT * FROM products WHERE scanId = :scanId ORDER BY timestamp DESC")
    fun getProductsByScanId(scanId: Long): LiveData<List<Product>>

    @Query("SELECT * FROM products WHERE upcCode = :upcCode LIMIT 1")
    fun getProductByUpcCode(upcCode: String): LiveData<Product?>

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)
}