package com.example.kotlinactivities.homePage

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
import com.example.kotlinactivities.network.Redirect
import com.example.kotlinactivities.network.RetrofitClient
import com.example.kotlinactivities.network.SourceAttributes
import com.example.kotlinactivities.network.SourceData
import com.example.kotlinactivities.network.SourceRequest
import com.example.kotlinactivities.network.SourceResponse

class PaymentNotImplementedActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var payButton: Button
    private lateinit var priceTextView: TextView

    private var totalPrice: Int = 0 // Total price to be displayed
    private var roomTitle: String? = null // Title of the room to be passed to home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_not_implemented)

        // Initialize UI components
        webView = findViewById(R.id.paymentWebView)
        payButton = findViewById(R.id.payButton)
        priceTextView = findViewById(R.id.priceTextView)

        // Retrieve total price and room title from the intent
        totalPrice = intent.getIntExtra("totalPrice", 0)
        roomTitle = intent.getStringExtra("roomTitle")

        updatePriceDisplay()

        // Set up Pay Button click listener
        payButton.setOnClickListener {
            if (totalPrice > 0) {
                createPayMongoSource(totalPrice)
            } else {
                showToast("Error: Total price is not valid.")
            }
        }

        // Configure WebView
        configureWebView()
    }

    private fun updatePriceDisplay() {
        // Format the price and display it in the TextView
        priceTextView.text = "Total Price: ₱${totalPrice / 100}" // Divide by 100 if amount is in centavos
    }

    private fun configureWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        // Set up WebViewClient to handle custom URLs
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                if (url.startsWith("https://waveaway.scarlet2.io/")) {
                    // Handle success or failure
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
                super.onPageFinished(view, url)

                // Optionally handle messages directly here for specific pages
                if (url.contains("success.html")) {
                    handlePaymentSuccess()
                } else if (url.contains("failure.html")) {
                    showToast("Payment failed or was canceled.")
                }
            }

            override fun onReceivedError(view: WebView, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                showToast("Failed to load page: $description")
            }
        }
    }

    private fun handlePaymentSuccess() {
        showToast("Payment successful. Booking awaiting approval.")

        // Redirect back to home with the selected room's details
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("navigateTo", "MyRoomFragment")
        intent.putExtra("roomTitle", roomTitle) // Example room title
        intent.putExtra("totalPrice", totalPrice) // Total price
        startActivity(intent)

        // Finish this activity
        finish()
    }

    private fun createPayMongoSource(amount: Int) {
        // Create the SourceRequest object
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

        // Make the API call to PayMongo
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
        // Load the PayMongo checkout URL in WebView
        webView.visibility = View.VISIBLE
        webView.loadUrl(url)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
