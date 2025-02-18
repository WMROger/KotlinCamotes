package com.example.kotlinactivities.userPage.myRoom

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.kotlinactivities.R
import com.example.kotlinactivities.databinding.FragmentCancelBookingBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.FirebaseDatabase

class CancelBookingFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCancelBookingBinding? = null
    private val binding get() = _binding!!

    private var roomId: String? = null
    private var onDismissListener: OnDismissListener? = null
    private var isCancelled: Boolean = false

    interface OnDismissListener {
        fun onDialogDismissed()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnDismissListener) {
            onDismissListener = context
        }
    }

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

        // Check if the room is already cancelled
        checkBookingStatus()
    }

    private fun cancelBooking() {
        roomId?.let {
            val databaseReference = FirebaseDatabase.getInstance().getReference("bookings")
            val bookingReference = databaseReference.child(it)

            // Update the paymentStatus to "Cancelled"
            bookingReference.child("paymentStatus").setValue("Cancelled")
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Booking cancelled successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                    isCancelled = true
                    updateCancelButtonState()
                    dismiss() // Close the fragment after cancellation
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Failed to cancel booking: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } ?: run {
            Toast.makeText(requireContext(), "Invalid room ID.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkBookingStatus() {
        roomId?.let {
            val databaseReference = FirebaseDatabase.getInstance().getReference("bookings").child(it)

            databaseReference.child("paymentStatus").get().addOnSuccessListener { snapshot ->
                val paymentStatus = snapshot.getValue(String::class.java) ?: ""
                isCancelled = paymentStatus.equals("Cancelled", ignoreCase = true)
                updateCancelButtonState()
            }
        }
    }

    private fun updateCancelButtonState() {
        if (isCancelled) {
            binding.cancelBookingButton.isEnabled = false
            binding.cancelBookingButton.text = "Already Cancelled"
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
