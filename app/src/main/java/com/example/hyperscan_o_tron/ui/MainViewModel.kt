package com.example.hyperscan_o_tron.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.hyperscan_o_tron.data.AppDatabase
import com.example.hyperscan_o_tron.data.Product
import com.example.hyperscan_o_tron.data.Scan
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val scanDao = AppDatabase.getDatabase(application).scanDao()
    private val productDao = AppDatabase.getDatabase(application).productDao()

    val allScans: LiveData<List<Scan>> = scanDao.getAllScans()


    fun createScan(scan: Scan) {
        viewModelScope.launch {
            scanDao.insertScan(scan)
        }
    }

    fun deleteScan(scan: Scan) {
        viewModelScope.launch {
            scanDao.deleteScan(scan)
        }
    }

    fun getScanById(scanId: Long): LiveData<Scan> {
        return scanDao.getScanById(scanId)
    }

    // Get products for a specific scan
    fun getProductsForScan(scanId: Long): LiveData<List<Product>> {
        return productDao.getProductsByScanId(scanId)
    }

    // Delete a scan
    fun deleteScan(scanId: Long) {
        viewModelScope.launch {
            val scan = scanDao.getScanById(scanId).value
            if (scan != null) {
                scanDao.deleteScan(scan)
            }
        }
    }
}
