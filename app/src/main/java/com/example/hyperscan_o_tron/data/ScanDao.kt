package com.example.hyperscan_o_tron.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ScanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: Scan): Long

    @Update
    suspend fun updateScan(scan: Scan)

    @Delete
    suspend fun deleteScan(scan: Scan)

    @Query("SELECT * FROM scans ORDER BY createdAt DESC")
    fun getAllScans(): LiveData<List<Scan>>

    @Query("SELECT * FROM scans WHERE id = :scanId")
    fun getScanById(scanId: Long): LiveData<Scan>

    @Query("SELECT * FROM scans WHERE id = :scanId")
    suspend fun getScanByIdSync(scanId: Long): Scan?
}
