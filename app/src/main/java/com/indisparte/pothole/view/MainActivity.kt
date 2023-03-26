package com.indisparte.pothole.view

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.indisparte.pothole.data.network.PotholeRepository
import com.indisparte.pothole.databinding.ActivityMainBinding
import com.indisparte.pothole.util.Constant
import com.indisparte.pothole.util.PermissionManager
import com.indisparte.pothole.view.MainActivity
import com.indisparte.pothole.view.viewModel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val sharedViewModel: SharedViewModel by viewModels()
    private lateinit var preferences: SharedPreferences
    private var pressedTime: Long = 0
    private lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var potholeRepository: PotholeRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        permissionManager = PermissionManager(
            this,
            Constant.permissions,
            Constant.LOCATION_REQUEST_CODE
        ) { result ->
            if (result.values.all { it }) {
                // All permissions are granted
                sharedViewModel.setIsPermissionGranted(true)

            } else {
                // Some permissions are not granted
                sharedViewModel.setIsPermissionGranted(false)
            }
        }
        permissionManager.checkPermissions()
        setContentView(binding.root)
        loadSettings()
    }

    private fun loadSettings() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val dark_theme = preferences.getBoolean(Constant.DARK_MODE_PRECISION_KEY, false)
        AppCompatDelegate.setDefaultNightMode(
            if (dark_theme) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.handlePermissionsResult(requestCode, permissions, grantResults)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            try {
                potholeRepository.closeConnection()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            finish()
        } else {
            Toast.makeText(this, "Press again to close app", Toast.LENGTH_SHORT).show()
        }
        pressedTime = System.currentTimeMillis()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
