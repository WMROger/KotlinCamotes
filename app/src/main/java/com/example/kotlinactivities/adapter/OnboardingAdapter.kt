package com.example.kotlinactivities.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R

class OnboardingAdapter(private val context: Context) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    // Define the layouts for each slide
    private val layouts = listOf(
        R.layout.splash_screen1,
        R.layout.splash_screen2,
        R.layout.splash_screen3
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(context).inflate(layouts[viewType], parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        // No binding needed for static layouts
    }

    override fun getItemCount(): Int = layouts.size

    override fun getItemViewType(position: Int): Int = position

    inner class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
