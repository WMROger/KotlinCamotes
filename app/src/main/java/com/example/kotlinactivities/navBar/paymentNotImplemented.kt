package com.example.kotlinactivities.navBar

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinactivities.R
import com.example.kotlinactivities.network.PaymentIntentAttributes
import com.example.kotlinactivities.network.PaymentIntentData
import com.example.kotlinactivities.network.PaymentIntentRequest
import com.example.kotlinactivities.network.PaymentIntentResponse
import com.example.kotlinactivities.network.Redirect
import com.example.kotlinactivities.network.RetrofitClient
import com.example.kotlinactivities.network.SourceAttributes
import com.example.kotlinactivities.network.SourceData
import com.example.kotlinactivities.network.SourceRequest
import com.example.kotlinactivities.network.SourceResponse

class paymentNotImplemented : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var payButton: Button
    private lateinit var amountSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_not_implemented)

        // Initialize UI Components
        payButton = findViewById(R.id.payButton)
        amountSpinner = findViewById(R.id.amountSpinner)
        webView = findViewById(R.id.paymentWebView)

        // Set up the Pay Button click listener
        payButton.setOnClickListener {
            val selectedAmount = when (amountSpinner.selectedItem.toString()) {
                "PHP 1000" -> 1000 * 100 // Amount in centavos
                "PHP 3000" -> 3000 * 100
                else -> 0
            }

            if (selectedAmount > 0) {
                initiateGcashPayment(selectedAmount, "09171234567") // Replace with actual phone
            } else {
                showToast("Please select a valid amount.")
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
                        success = "https://httpbin.org/anything?status=success",
                        failed = "https://httpbin.org/anything?status=failed"
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
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                toggleUIVisibility(true)
            }

            override fun onReceivedError(view: WebView, errorCode: Int, description: String?, failingUrl: String?) {
                showToast("Failed to load page: $description")
            }
        }

        toggleUIVisibility(true)
        webView.loadUrl(url)
    }

    private fun toggleUIVisibility(showWebView: Boolean) {
        webView.visibility = if (showWebView) View.VISIBLE else View.GONE
        payButton.visibility = if (showWebView) View.GONE else View.VISIBLE
        amountSpinner.visibility = if (showWebView) View.GONE else View.VISIBLE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
