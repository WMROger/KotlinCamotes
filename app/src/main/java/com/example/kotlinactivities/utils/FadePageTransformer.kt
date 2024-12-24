package com.example.kotlinactivities.utils

import android.view.View
import androidx.viewpager2.widget.ViewPager2

class FadePageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        when {
            position <= -1f || position >= 1f -> {
                // Pages out of view are completely transparent
                view.alpha = 0f
            }
            position == 0f -> {
                // The active page is fully opaque
                view.alpha = 1f
            }
            else -> {
                // Fade the page proportional to its distance from the center
                view.alpha = 1f - Math.abs(position)
            }
        }
    }
}
