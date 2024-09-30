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


    fun createScan(scan: Scan, onScanCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val scanId = scanDao.insertScan(scan)
            onScanCreated(scanId)
        }
    }

    fun updateScanFolder(scanId: Long, folderPath: String) {
        viewModelScope.launch {
            val scan = scanDao.getScanByIdSync(scanId)
            if (scan != null) {
                scan.folderPath = folderPath
                scanDao.updateScan(scan)
            }
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

    suspend fun getScanByIdSync(scanId: Long): Scan? {
        return scanDao.getScanByIdSync(scanId)
    }

    fun insertProduct(product: Product) {
        viewModelScope.launch {
            productDao.insertProduct(product)
        }
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

    fun getProductByUpcCode(upcCode: String): LiveData<Product?> {
        return productDao.getProductByUpcCode(upcCode)
    }

    // Update product details (e.g., after retaking an image)
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            productDao.updateProduct(product)
        }
    }
}
