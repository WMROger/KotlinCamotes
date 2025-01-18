package com.example.kotlinactivities.userPage.homePage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinactivities.MainActivity
import com.example.kotlinactivities.R
import com.example.kotlinactivities.model.Room
import com.example.kotlinactivities.network.Redirect
import com.example.kotlinactivities.network.RetrofitClient
import com.example.kotlinactivities.network.SourceAttributes
import com.example.kotlinactivities.network.SourceData
import com.example.kotlinactivities.network.SourceRequest
import com.example.kotlinactivities.network.SourceResponse
import com.example.kotlinactivities.utils.RoomManager
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class PaymentNotImplementedActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var payButton: Button
    private lateinit var priceTextView: TextView

    private var totalPrice: Int = 0
    private var roomTitle: String? = null
    private var guestCount: Int = 0
    private var roomPrice: Int = 0
    private var imageUrl: String = "https://waveaway.scarlet2.io/assets/ic_cupids_deluxe.png" // Default image URL
    private var startDate: Date? = null
    private var endDate: Date? = null

    private val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_not_implemented)

        // Initialize UI components
        webView = findViewById(R.id.paymentWebView)
        payButton = findViewById(R.id.payButton)
        priceTextView = findViewById(R.id.priceTextView)

        // Retrieve booking details from the intent
        totalPrice = intent.getIntExtra("totalPrice", 0)
        roomTitle = intent.getStringExtra("roomTitle")
        guestCount = intent.getIntExtra("guestCount", 0)
        roomPrice = intent.getIntExtra("roomPrice", 0)
        imageUrl = intent.getStringExtra("imageUrl") ?: "https://waveaway.scarlet2.io/assets/ic_cupids_deluxe.png"

        val startDateMillis = intent.getLongExtra("startDate", 0L)
        val endDateMillis = intent.getLongExtra("endDate", 0L)
        if (startDateMillis != 0L) startDate = Date(startDateMillis)
        if (endDateMillis != 0L) endDate = Date(endDateMillis)

        Log.d(
            "PaymentActivity",
            "RoomTitle: $roomTitle, TotalPrice: $totalPrice, GuestCount: $guestCount, RoomPrice: $roomPrice, ImageUrl: $imageUrl, StartDate: $startDate, EndDate: $endDate"
        )

        // Update price display
        updatePriceDisplay()

        // Configure WebView
        configureWebView()

        // Set Pay button action
        payButton.setOnClickListener {
            if (totalPrice > 0) {
                createPayMongoSource(totalPrice)
            } else {
                showToast("Error: Total price is not valid.")
            }
        }
    }

    private fun updatePriceDisplay() {
        val startDateString = if (startDate != null) dateFormatter.format(startDate!!) else "N/A"
        val endDateString = if (endDate != null) dateFormatter.format(endDate!!) else "N/A"
        priceTextView.text = "Total Price: ₱${totalPrice / 100}\nStart Date: $startDateString\nEnd Date: $endDateString"
    }

    private fun configureWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                if (url.startsWith("https://waveaway.scarlet2.io/")) {
                    if (url.contains("success.html")) {
                        handlePaymentSuccess()
                    } else if (url.contains("failure.html")) {
                        showToast("Payment failed or was canceled.")
                        finish()
                    }
                    return true
                }
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                if (url.contains("success.html")) {
                    handlePaymentSuccess()
                } else if (url.contains("failure.html")) {
                    showToast("Payment failed or was canceled.")
                }
            }

            override fun onReceivedError(view: WebView, errorCode: Int, description: String?, failingUrl: String?) {
                showToast("Failed to load page: $description")
            }
        }
    }

    private fun handlePaymentSuccess() {
        val bookingId = intent.getStringExtra("bookingId") // Retrieve booking ID
        if (bookingId != null) {
            val database = FirebaseDatabase.getInstance()
            val bookingsRef = database.getReference("bookings").child(bookingId)

            // Update the paymentStatus to "Success"
            bookingsRef.child("paymentStatus").setValue("Success")
                .addOnSuccessListener {
                    Log.d("PaymentActivity", "Payment status updated to Success.")
                    Toast.makeText(this, "Payment successful!", Toast.LENGTH_LONG).show()

                    // Navigate to MyRoomFragment
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("navigateTo", "MyRoomFragment")
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { error ->
                    Log.e("PaymentActivity", "Failed to update payment status: ${error.message}")
                    Toast.makeText(
                        this,
                        "Failed to update payment status. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
        } else {
            Toast.makeText(this, "Booking ID not found. Cannot update payment status.", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadToFirebase(
        roomTitle: String,
        userId: String,
        userEmail: String,
        firstImageUrl: String,
        paymentMethod: String
    ) {
        val database = FirebaseDatabase.getInstance()
        val bookingsRef = database.getReference("bookings")

        val bookingId = bookingsRef.push().key // Generate a unique booking ID
        if (bookingId != null) {
            val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val totalDays = intent.getIntExtra("totalDays", 1)
            val startDateMillis = intent.getLongExtra("startDate", 0L)
            val endDateMillis = intent.getLongExtra("endDate", 0L)

            // Safely parse startDate and endDate
            val startDate = if (startDateMillis != 0L) Date(startDateMillis) else null
            val endDate = if (endDateMillis != 0L) Date(endDateMillis) else null

            val bookingData = mapOf(
                "userId" to userId,
                "userEmail" to userEmail,
                "roomTitle" to roomTitle,
                "roomPrice" to roomPrice,
                "guestCount" to guestCount,
                "totalPrice" to totalPrice / 100, // Convert from centavos to pesos
                "totalDays" to totalDays,
                "startDate" to startDate?.time,
                "startDateReadable" to startDate?.let { dateFormatter.format(it) },
                "endDate" to endDate?.time,
                "endDateReadable" to endDate?.let { dateFormatter.format(it) },
                "imageUrl" to firstImageUrl,
                "paymentMethod" to paymentMethod,
                "paymentStatus" to "Success"
            )

            bookingsRef.child(bookingId).setValue(bookingData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Firebase", "Booking uploaded successfully.")
                    } else {
                        Log.e("Firebase", "Failed to upload booking: ${task.exception?.message}")
                        showToast("Error uploading booking details to Firebase.")
                    }
                }
        }
    }

    private fun createPayMongoSource(amount: Int) {
        val sourceRequest = SourceRequest(
            data = SourceData(
                attributes = SourceAttributes(
                    amount = amount,
                    currency = "PHP",
                    type = "gcash",
                    redirect = Redirect(
                        success = "https://waveaway.scarlet2.io/success.html",
                        failed = "https://waveaway.scarlet2.io/failure.html"
                    )
                )
            )
        )

        RetrofitClient.instance.createSource(sourceRequest).enqueue(object : retrofit2.Callback<SourceResponse> {
            override fun onResponse(call: retrofit2.Call<SourceResponse>, response: retrofit2.Response<SourceResponse>) {
                if (response.isSuccessful) {
                    val checkoutUrl = response.body()?.data?.attributes?.redirect?.checkout_url
                    if (checkoutUrl != null) {
                        openCheckoutPage(checkoutUrl)
                    } else {
                        showToast("Error: Missing checkout URL in response.")
                    }
                } else {
                    showToast("Error: PayMongo API returned ${response.code()}.")
                }
            }

            override fun onFailure(call: retrofit2.Call<SourceResponse>, t: Throwable) {
                showToast("Failed to create payment source: ${t.message}")
            }
        })
    }

    private fun openCheckoutPage(url: String) {
        webView.visibility = View.VISIBLE
        webView.loadUrl(url)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
