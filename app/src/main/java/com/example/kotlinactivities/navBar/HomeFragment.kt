package com.example.kotlinactivities.navBar

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.adapter.RoomAdapter
import com.example.kotlinactivities.model.Room
import com.example.kotlinactivities.R

class HomeFragment : Fragment() {

    private lateinit var roomsRecyclerView: RecyclerView
    private lateinit var roomAdapter: RoomAdapter
    private val roomList = mutableListOf<Room>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize RecyclerView
        roomsRecyclerView = view.findViewById(R.id.roomsRecyclerView)
        roomsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize RoomAdapter with data and set it to the RecyclerView
        roomAdapter = RoomAdapter(roomList)
        roomsRecyclerView.adapter = roomAdapter

        // Load room data
        loadRoomData()

        return view
    }

    private fun loadRoomData() {
        val context = requireContext()

        // Add mock data with dynamically constructed URI
        roomList.add(
            Room(
                imageUrl = Uri.parse("android.resource://${context.packageName}/drawable/ic_home").toString(),
                title = "Deluxe Room",
                people = "2",
                price = "₱1,678/night",
                rating = "4.9"
            )
        )

        roomList.add(
            Room(
                imageUrl = Uri.parse("android.resource://${context.packageName}/drawable/ic_home").toString(),
                title = "Barkada Room",
                people = "5",
                price = "₱2,500/night",
                rating = "4.8"
            )
        )

        roomList.add(
            Room(
                imageUrl = Uri.parse("android.resource://${context.packageName}/drawable/ic_home").toString(),
                title = "Regular Room",
                people = "3",
                price = "₱1,200/night",
                rating = "4.5"
            )
        )

        // Log data
        for (room in roomList) {
            println("Loaded Room: ${room.title}, ${room.price}")
        }

        // Notify the adapter about data changes
        roomAdapter.notifyDataSetChanged()

        Log.d("HomeFragment", "Room list size: ${roomList.size}")
    }
}
