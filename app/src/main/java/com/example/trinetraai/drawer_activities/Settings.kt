package com.example.trinetraai.drawer_activities

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import com.example.trinetraai.R
import com.example.trinetraai.authentication.ForgotPass
import com.example.trinetraai.authentication.Login
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class Settings : AppCompatActivity() {

    private lateinit var switchDarkMode: SwitchCompat
    private lateinit var switchNotifications: SwitchCompat
    private lateinit var prefs: SharedPreferences
    private lateinit var btnChangePassword: MaterialButton
    private lateinit var btnLogoutSettings: MaterialButton


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            prefs.edit().putBoolean("notifications", true).apply()
            Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
            switchNotifications.isChecked = true
        } else {
            prefs.edit().putBoolean("notifications", false).apply()
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            switchNotifications.isChecked = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        // Initialize views
        switchDarkMode = findViewById(R.id.switchDarkMode)
        switchNotifications = findViewById(R.id.switchNotifications)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnLogoutSettings = findViewById(R.id.btnLogoutSettings)
        prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)

        // Load saved states
        val isDark = prefs.getBoolean("dark_mode", false)
        val isNotify = prefs.getBoolean("notifications", false)

        switchDarkMode.isChecked = isDark
        switchNotifications.isChecked = isNotify

        // Dark Mode Toggle
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        btnChangePassword.setOnClickListener {
            val intent = Intent(this, ForgotPass::class.java)
            startActivity(intent)
        }

        btnLogoutSettings.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Notifications Toggle
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // Ask permission
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        // Already granted
                        prefs.edit().putBoolean("notifications", true).apply()
                        Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Android 12 and below
                    prefs.edit().putBoolean("notifications", true).apply()
                    Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
                }
            } else {
                prefs.edit().putBoolean("notifications", false).apply()
                Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
