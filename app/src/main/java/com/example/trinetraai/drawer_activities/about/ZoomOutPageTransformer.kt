package com.example.trinetraai.drawer_activities.about

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class ZoomOutPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        val scale = 0.85f.coerceAtLeast(1 - abs(position))
        view.scaleY = scale
        view.scaleX = scale
        view.alpha = 0.5f + (1 - abs(position))
        view.translationX = -position * view.width * 0.2f
    }
}