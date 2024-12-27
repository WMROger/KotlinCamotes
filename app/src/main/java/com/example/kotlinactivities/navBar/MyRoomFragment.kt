package com.example.kotlinactivities.navBar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinactivities.R
import com.example.kotlinactivities.adapter.MyRoomsAdapter
import com.example.kotlinactivities.model.Room

class MyRoomFragment : Fragment() {

    private lateinit var myRoomsRecyclerView: RecyclerView
    private lateinit var myRoomsAdapter: MyRoomsAdapter

    private val myRoomsList = mutableListOf<Room>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_room, container, false)

        // Initialize RecyclerView
        myRoomsRecyclerView = view.findViewById(R.id.myRoomsRecyclerView)
        myRoomsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        myRoomsAdapter = MyRoomsAdapter(myRoomsList) { room ->
            room.isFavorited = !room.isFavorited
            myRoomsAdapter.notifyDataSetChanged()
        }
        myRoomsRecyclerView.adapter = myRoomsAdapter

        // Retrieve arguments
        val roomTitle = arguments?.getString("roomTitle")
        val totalPrice = arguments?.getInt("totalPrice")

        // Add the room to the list if arguments exist
        if (roomTitle != null && totalPrice != null) {
            myRoomsList.add(
                Room(
                    imageUrl = R.drawable.ic_cupids_deluxe, // Replace with actual drawable
                    title = roomTitle,
                    people = "2 People",
                    price = "₱${totalPrice / 100}/night",
                    rating = "4.9",
                    isFavorited = false
                )
            )
            myRoomsAdapter.notifyDataSetChanged()
        }

        return view
    }

}
