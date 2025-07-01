package com.example.trinetraai.navigationScreen

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trinetraai.R
import com.google.android.material.imageview.ShapeableImageView

class Nav_PatrolRoute : AppCompatActivity() {
    private lateinit var gestureDetector: GestureDetectorCompat
    private lateinit var patrolNxt : ShapeableImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nav_patrol_route)

        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener())

        patrolNxt = findViewById(R.id.patrol_nextbtn)
        patrolNxt.setOnClickListener {
            startActivity(Intent(this, Nav_WomenSafety::class.java))
        }
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
            velocityY: Float
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
        val intent = Intent(this, Nav_Heatmap::class.java)
        startActivity(intent)
        finish()
    }

    private fun onSwipeLeft() {
        val intent = Intent(this, Nav_WomenSafety::class.java)
        startActivity(intent)
        finish()
    }
}