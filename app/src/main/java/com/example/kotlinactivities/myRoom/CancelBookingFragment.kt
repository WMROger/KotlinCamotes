package com.example.kotlinactivities.myRoom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.kotlinactivities.R
import com.example.kotlinactivities.databinding.FragmentCancelBookingBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CancelBookingFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCancelBookingBinding? = null
    private val binding get() = _binding!!

    private var roomId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCancelBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get room ID from arguments
        roomId = arguments?.getString("roomId")

        // Handle Cancel booking confirmation
        binding.cancelBookingButton.setOnClickListener {
            cancelBooking()
        }
    }

    private fun cancelBooking() {
        roomId?.let {
            val databaseReference = FirebaseDatabase.getInstance().getReference("bookings")
            databaseReference.child(it).removeValue() // Remove booking from Firebase
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Booking canceled successfully.", Toast.LENGTH_SHORT).show()
                    dismiss() // Close the fragment after cancellation
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to cancel booking: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(roomId: String): CancelBookingFragment {
            val fragment = CancelBookingFragment()
            val args = Bundle()
            args.putString("roomId", roomId)
            fragment.arguments = args
            return fragment
        }
    }
}
