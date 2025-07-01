package com.example.trinetraai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trinetraai.authentication.Login
import com.example.trinetraai.navigationScreen.Nav_Heatmap
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            startActivity(Intent(this, LandingDashboard::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val videoView = findViewById<VideoView>(R.id.videoView)
        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.trinetra}")
        videoView.setVideoURI(videoUri)
        videoView.setOnCompletionListener {
            return@setOnCompletionListener
        }
        videoView.start()
        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("OnboardingPrefs", MODE_PRIVATE)
            val onboardingShown = prefs.getBoolean("onboarding_shown", false)

            if (onboardingShown) {
                startActivity(Intent(this, Login::class.java))
            } else {
                startActivity(Intent(this, Nav_Heatmap::class.java))
            }
            finish()
        }, 2000)

    }
}