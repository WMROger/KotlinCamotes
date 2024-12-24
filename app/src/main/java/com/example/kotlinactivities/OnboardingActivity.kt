package com.example.kotlinactivities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.kotlinactivities.adapter.OnboardingAdapter

class OnboardingActivity : AppCompatActivity() {

    private var delayHandler: Handler? = null // For delayed button visibility
    private lateinit var indicatorDots: Array<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val btnGetStarted = findViewById<View>(R.id.btnGetStarted)
        val indicatorLayout = findViewById<View>(R.id.indicator_layout) as ViewGroup

        // Initialize the adapter and set it to the ViewPager
        val adapter = OnboardingAdapter(this)
        viewPager.adapter = adapter

        // Create and set up indicator dots dynamically
        indicatorDots = Array(adapter.itemCount) { index ->
            val dot = ImageView(this).apply {
                setImageResource(if (index == 0) R.drawable.dot_active else R.drawable.dot_inactive)
                layoutParams = LinearLayout.LayoutParams(
                    30, // Default width for unselected dots (20dp converted to pixels)
                    30  // Default height for all dots (20dp converted to pixels)
                ).apply {
                    setMargins(8, 0, 8, 0) // Spacing between dots
                }
            }
            indicatorLayout.addView(dot)
            dot
        }

        // Initially hide the button
        btnGetStarted.visibility = View.GONE

        // Set up the handler for delayed visibility
        delayHandler = Handler(Looper.getMainLooper())

        // Listen for page changes in the ViewPager2
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // Update the dots
                updateIndicator(position)

                // Check if the user is on the last slide
                if (position == adapter.itemCount - 1) {
                    // Delay the button's appearance by 2 seconds
                    delayHandler?.postDelayed({
                        btnGetStarted.visibility = View.VISIBLE
                    }, 1500)
                } else {
                    // Hide the button on other slides and cancel pending delays
                    btnGetStarted.visibility = View.GONE
                    delayHandler?.removeCallbacksAndMessages(null)
                }
            }
        })

        // Handle button click
        btnGetStarted.setOnClickListener {
            // Navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close the onboarding activity
        }
    }

    private fun updateIndicator(position: Int) {
        for (i in indicatorDots.indices) {
            val params = indicatorDots[i].layoutParams as LinearLayout.LayoutParams
            if (i == position) {
                // Set the selected dot to be longer in width
                params.width = 80 // Selected dot width (50dp converted to pixels)
                params.height = 30 // Keep the height same
                indicatorDots[i].setImageResource(R.drawable.dot_active) // Active dot drawable
            } else {
                // Reset the unselected dots to default size
                params.width = 30 // Default dot width (20dp converted to pixels)
                params.height = 30 // Default dot height
                indicatorDots[i].setImageResource(R.drawable.dot_inactive) // Inactive dot drawable
            }
            indicatorDots[i].layoutParams = params
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up the handler to avoid memory leaks
        delayHandler?.removeCallbacksAndMessages(null)
    }
}
