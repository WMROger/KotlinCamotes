package com.example.kotlinactivities.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import com.example.kotlinactivities.R

class OnboardingAdapter(private val context: Context) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    // Define the data for each slide
    private val slides = listOf(
        Slide(R.layout.splash_screen1),
        Slide(R.layout.splash_screen2),
        Slide(R.layout.splash_screen3)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(context).inflate(slides[viewType].layoutRes, parent, false)
        return OnboardingViewHolder(view)
    }
    data class Slide(val layoutRes: Int)

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        // Future: Add logic for dynamic binding (if needed)
    }

    override fun getItemCount(): Int = slides.size

    override fun getItemViewType(position: Int): Int = position

    inner class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
