package com.example.trinetraai.navigationScreen

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trinetraai.R
import com.example.trinetraai.authentication.Login
import com.google.android.material.imageview.ShapeableImageView
import androidx.core.content.edit

class Nav_WomenSafety : AppCompatActivity() {
    private lateinit var gestureDetector: GestureDetectorCompat
    private lateinit var womenSafetyNxt : ShapeableImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        setContentView(R.layout.activity_nav_women_safety)

        womenSafetyNxt = findViewById(R.id.women_nextbtn)

        womenSafetyNxt.setOnClickListener {
            val prefs = getSharedPreferences("OnboardingPrefs", MODE_PRIVATE)
            prefs.edit { putBoolean("onboarding_shown", true) }

            startActivity(Intent(this, Login::class.java))
            finish()

        }


        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener())
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    private inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {

        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float,
        ): Boolean {
            if (e1 == null || e2 == null) return false
            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            return if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight()
                    } else {
                        onSwipeLeft()
                    }
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }

    private fun onSwipeRight() {
        val intent = Intent(this, Nav_PatrolRoute::class.java)
        startActivity(intent)
    }

    private fun onSwipeLeft() {
        val prefs = getSharedPreferences("OnboardingPrefs", MODE_PRIVATE)
        prefs.edit { putBoolean("onboarding_shown", true) }
        startActivity(Intent(this, Login::class.java))
        finish()

    }
}