package com.example.hyperscan_o_tron.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import com.example.hyperscan_o_tron.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val PREFS_NAME = "app_prefs"
    private val KEY_STORAGE_URI = "storage_uri"

    private var storageUri: Uri? = null

    // Register an ActivityResultLauncher for the folder picker
    private val folderPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleFolderPickerResult(result.resultCode, result.data)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Load the current storage URI
        loadStorageUri()

        // Display the current storage path
        displayCurrentStoragePath()

        // Set up the button to change the storage path
        binding.changeStorageButton.setOnClickListener {
            openFolderPicker()
        }
    }

    private fun loadStorageUri() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uriString = prefs.getString(KEY_STORAGE_URI, null)
        storageUri = uriString?.let { Uri.parse(it) }
    }

    private fun displayCurrentStoragePath() {
        val path = if (storageUri != null) {
            getPathFromUri(storageUri!!)
        } else {
            "Default App Storage"
        }
        binding.currentStoragePathTextView.text = "Current Storage Path:\n$path"
    }

    private fun openFolderPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                        Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            )
        }
        folderPickerLauncher.launch(intent)
    }

    @SuppressLint("WrongConstant")
    private fun handleFolderPickerResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data ?: return
            // Persist permissions
            val contentResolver = requireContext().contentResolver
            val takeFlags = data.flags and
                    (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            contentResolver.takePersistableUriPermission(uri, takeFlags)

            // Save the URI
            storageUri = uri
            saveStorageUri(uri)

            // Update the displayed path
            displayCurrentStoragePath()

            Toast.makeText(requireContext(), "Storage location updated.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Storage location not changed.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun saveStorageUri(uri: Uri) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_STORAGE_URI, uri.toString()).apply()
    }

    private fun getPathFromUri(uri: Uri): String {
        val documentFile = DocumentFile.fromTreeUri(requireContext(), uri)
        return documentFile?.name ?: "Unknown Location"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}