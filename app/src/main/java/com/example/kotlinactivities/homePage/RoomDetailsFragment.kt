package com.example.kotlinactivities.homePage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.kotlinactivities.R
import com.example.kotlinactivities.databinding.FragmentRoomDetailsBinding
import com.example.kotlinactivities.model.Room

class RoomDetailsFragment : Fragment() {

    private var _binding: FragmentRoomDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRoomDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get Room data from arguments
        val room = arguments?.getParcelable<Room>("room")

        // Populate Room Details
        room?.let {
            binding.roomImage.setImageResource(it.imageUrl)
            binding.roomTitle.text = it.title
            binding.roomLocation.text = "Himensulan Island, Camotes Cebu" // Static for now
            binding.roomRating.text = it.rating
            binding.roomPrice.text = it.price
            binding.roomDescription.text =
                "Indulge in luxury and comfort in our Deluxe Room, featuring elegant interiors, plush bedding, a spacious seating area, and modern amenities."
        }

        // Book button action (placeholder)
        binding.bookButton.setOnClickListener {
            // Implement booking action
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
