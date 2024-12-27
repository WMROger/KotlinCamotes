package com.example.kotlinactivities.navBar

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
import com.example.kotlinactivities.R
import com.example.kotlinactivities.network.*

class PaymentNotImplementedActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var payButton: Button
    private lateinit var priceTextView: TextView

    private var totalPrice: Int = 0 // Total price to be displayed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_not_implemented)

        // Initialize UI components
        webView = findViewById(R.id.paymentWebView)
        payButton = findViewById(R.id.payButton)
        priceTextView = findViewById(R.id.priceTextView)

        // Retrieve total price from intent
        totalPrice = intent.getIntExtra("totalPrice", 0)
        updatePriceDisplay()

        // Set up Pay Button click listener
        payButton.setOnClickListener {
            if (totalPrice > 0) {
                initiateGcashPayment(totalPrice, "09171234567") // Replace with actual phone
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
                if (url.startsWith("myapp://home")) {
                    // Show a toast message based on the referrer (success or failure)
                    if (url.contains("success.html")) {
                        showToast("Booking awaiting approval")
                    } else if (url.contains("failure.html")) {
                        showToast("Booking denied or an error occurred")
                    }

                    // Close the WebView and return to the app
                    finish() // Ends the current activity
                    return true // Indicate that the URL was handled
                }
                return false // Allow WebView to handle other URLs
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                toggleUIVisibility(true)

                // Optionally handle messages directly here for specific pages
                if (url.contains("success.html")) {
                    showToast("Booking awaiting approval")
                } else if (url.contains("failure.html")) {
                    showToast("Booking denied or an error occurred")
                }
            }

            override fun onReceivedError(view: WebView, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                showToast("Failed to load page: $description")
            }
        }
    }

    private fun initiateGcashPayment(amount: Int, phone: String) {
        Log.d("PayWithGCash", "Initiating GCash payment with amount: $amount and phone: $phone")

        createPaymentIntent(amount) { clientKey, paymentIntentId ->
            Log.d("PayWithGCash", "Payment Intent created: clientKey=$clientKey, id=$paymentIntentId")

            createSource(amount, phone) { sourceCheckoutUrl ->
                Log.d("PayWithGCash", "Source created. Redirecting to URL: $sourceCheckoutUrl")
                openCheckoutPage(sourceCheckoutUrl)
            }
        }
    }

    private fun createPaymentIntent(amount: Int, callback: (String, String) -> Unit) {
        val paymentIntentRequest = PaymentIntentRequest(
            data = PaymentIntentData(
                attributes = PaymentIntentAttributes(amount = amount)
            )
        )

        RetrofitClient.instance.createPaymentIntent(paymentIntentRequest)
            .enqueue(object : retrofit2.Callback<PaymentIntentResponse> {
                override fun onResponse(
                    call: retrofit2.Call<PaymentIntentResponse>,
                    response: retrofit2.Response<PaymentIntentResponse>
                ) {
                    if (response.isSuccessful) {
                        val clientKey = response.body()?.data?.attributes?.client_key
                        val id = response.body()?.data?.id
                        if (clientKey != null && id != null) {
                            callback(clientKey, id)
                        } else {
                            showToast("Error: Missing clientKey or id in response.")
                        }
                    } else {
                        showToast("Error: Payment Intent API returned ${response.code()}.")
                    }
                }

                override fun onFailure(call: retrofit2.Call<PaymentIntentResponse>, t: Throwable) {
                    showToast("Failed to create Payment Intent: ${t.message}")
                }
            })
    }

    private fun createSource(amount: Int, phone: String, callback: (String) -> Unit) {
        val sourceRequest = SourceRequest(
            data = SourceData(
                attributes = SourceAttributes(
                    amount = amount,
                    redirect = Redirect(
                        success = "https://waveaway.scarlet2.io/success.html",
                        failed = "https://waveaway.scarlet2.io/failure.html"
                    ),
                    type = "gcash"
                )
            )
        )

        RetrofitClient.instance.createSource(sourceRequest)
            .enqueue(object : retrofit2.Callback<SourceResponse> {
                override fun onResponse(
                    call: retrofit2.Call<SourceResponse>,
                    response: retrofit2.Response<SourceResponse>
                ) {
                    if (response.isSuccessful) {
                        val checkoutUrl = response.body()?.data?.attributes?.redirect?.checkout_url
                        if (checkoutUrl != null) {
                            callback(checkoutUrl)
                        } else {
                            showToast("Error: Missing checkout URL in source response.")
                        }
                    } else {
                        showToast("Error: Source API returned ${response.code()}.")
                    }
                }

                override fun onFailure(call: retrofit2.Call<SourceResponse>, t: Throwable) {
                    showToast("Failed to create Source: ${t.message}")
                }
            })
    }

    private fun openCheckoutPage(url: String) {
        toggleUIVisibility(true)
        webView.loadUrl(url)
    }

    private fun toggleUIVisibility(showWebView: Boolean) {
        webView.visibility = if (showWebView) View.VISIBLE else View.GONE
        payButton.visibility = if (showWebView) View.GONE else View.VISIBLE
        priceTextView.visibility = if (showWebView) View.GONE else View.VISIBLE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
