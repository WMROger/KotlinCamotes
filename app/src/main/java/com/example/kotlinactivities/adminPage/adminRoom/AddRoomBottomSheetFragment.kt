package com.example.kotlinactivities.adminPage.adminRoom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.kotlinactivities.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddRoomBottomSheetFragment(
    private val onOptionClick: (Option) -> Unit // Callback to handle option clicks
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for the bottom sheet
        return inflater.inflate(R.layout.fragment_add_room_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set click listeners for the options
        view.findViewById<LinearLayout>(R.id.addCategoryOption).setOnClickListener {
            dismiss()
            onOptionClick(Option.ADD_CATEGORY)
        }

        view.findViewById<LinearLayout>(R.id.addRoomOption).setOnClickListener {
            dismiss()
            onOptionClick(Option.ADD_ROOM)
        }

        view.findViewById<LinearLayout>(R.id.addAmenitiesOption).setOnClickListener {
            dismiss()
            onOptionClick(Option.ADD_AMENITIES)
        }
    }

    // Enum for options
    enum class Option {
        ADD_CATEGORY,
        ADD_ROOM,
        ADD_AMENITIES
    }
}
