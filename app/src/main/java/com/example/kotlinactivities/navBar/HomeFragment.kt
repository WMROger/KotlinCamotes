package com.example.kotlinactivities.navBar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adapter.RoomAdapter
import com.example.kotlinactivities.model.Room

class HomeFragment : Fragment() {

    private lateinit var roomsRecyclerView: RecyclerView
    private lateinit var roomAdapter: RoomAdapter
    private val roomList = mutableListOf<Room>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Setup RecyclerView
        roomsRecyclerView = view.findViewById(R.id.roomsRecyclerView)
        roomsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        roomAdapter = RoomAdapter(roomList)
        roomsRecyclerView.adapter = roomAdapter

        // Load data
        loadRoomData()

        return view
    }

    private fun loadRoomData() {
        roomList.apply {
            add(
                Room(
                    imageUrl = R.drawable.ic_home,
                    title = "Deluxe Room",
                    people = "2",
                    price = "₱1,678/night",
                    rating = "4.9"
                )
            )
            add(
                Room(
                    imageUrl = R.drawable.ic_home,
                    title = "Barkada Room",
                    people = "5",
                    price = "₱2,500/night",
                    rating = "4.8"
                )
            )
            add(
                Room(
                    imageUrl = R.drawable.ic_home,
                    title = "Regular Room",
                    people = "3",
                    price = "₱1,200/night",
                    rating = "4.5"
                )
            )
        }
        Log.d("HomeFragment", "Room data loaded: ${roomList.size} items")
        roomAdapter.notifyDataSetChanged()
    }
}
