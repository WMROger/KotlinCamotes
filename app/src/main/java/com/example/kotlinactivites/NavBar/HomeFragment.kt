package com.example.kotlinactivites.NavBar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.kotlinactivites.Network.PaymentIntentRequest
import com.example.kotlinactivites.Network.PaymentIntentResponse
import com.example.kotlinactivites.Network.PaymentMethodRequest
import com.example.kotlinactivites.Network.PaymentMethodResponse
import com.example.kotlinactivites.Network.AttachPaymentRequest
import com.example.kotlinactivites.Network.PaymentResponse
import com.example.kotlinactivites.Network.PaymentDetails
import com.example.kotlinactivites.Network.PaymentIntentData
import com.example.kotlinactivites.Network.PaymentIntentAttributes
import com.example.kotlinactivites.Network.PaymentMethodData
import com.example.kotlinactivites.Network.PaymentMethodAttributes
import com.example.kotlinactivites.Network.AttachPaymentData
import com.example.kotlinactivites.Network.AttachPaymentAttributes
import com.example.kotlinactivites.Network.AttachSource
import com.example.kotlinactivites.Network.Redirect
import com.example.kotlinactivites.Network.SourceAttributes
import com.example.kotlinactivites.Network.SourceData
import com.example.kotlinactivites.Network.SourceRequest
import com.example.kotlinactivites.Network.SourceResponse
import com.example.kotlinactivites.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize button
        val payButton: Button = view.findViewById(R.id.payButton)
        payButton.setOnClickListener {
            Log.d("PayWithGCash", "Button clicked")
            try {
                initiateGcashPayment(10000, "09171234567")
            } catch (e: Exception) {
                Log.e("PayWithGCash", "Error occurred: ${e.message}", e)
            }
        }


        return view
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

    private fun createSource(amount: Int, phone: String, callback: (String) -> Unit) {
        Log.d("PayWithGCash", "Creating Source for GCash payment")

        val sourceRequest = SourceRequest(
            data = SourceData(
                attributes = SourceAttributes(
                    amount = amount,
                    redirect = Redirect(
                        success = "https://your-app.com/success",
                        failed = "https://your-app.com/failed"
                    ),
                    type = "gcash" // Specify the source type as "gcash"
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
                            Log.e("PayWithGCash", "Source creation failed: Missing checkout URL")
                            showToast("Source creation failed: Missing checkout URL")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("PayWithGCash", "Source API Error: ${response.code()} - $errorBody")
                        showToast("Source API Error: $errorBody")
                    }
                }

                override fun onFailure(call: retrofit2.Call<SourceResponse>, t: Throwable) {
                    Log.e("PayWithGCash", "Source API Failure: ${t.message}", t)
                    showToast("Network error: ${t.message}")
                }
            })
    }

    private fun createPaymentIntent(amount: Int, callback: (String, String) -> Unit) {
        Log.d("PayWithGCash", "Creating Payment Intent with amount: $amount")

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
                            Log.d("PayWithGCash", "Payment Intent created: clientKey=$clientKey, id=$id")
                            callback(clientKey, id)
                        } else {
                            val error = "Missing clientKey or id in response"
                            Log.e("PayWithGCash", error)
                            showToast(error)
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("PayWithGCash", "Payment Intent API Error: ${response.code()} - $errorBody")
                        showToast("Payment Intent Error: $errorBody")
                    }
                }

                override fun onFailure(call: retrofit2.Call<PaymentIntentResponse>, t: Throwable) {
                    Log.e("PayWithGCash", "Payment Intent API Failure: ${t.message}", t)
                    showToast("Network error: ${t.message}")
                }
            })
    }

    private fun createPaymentMethod(phone: String, callback: (String) -> Unit) {
        Log.d("PayWithGCash", "Creating Payment Method for phone: $phone")

        val paymentMethodRequest = PaymentMethodRequest(
            data = PaymentMethodData(
                attributes = PaymentMethodAttributes(
                    details = PaymentDetails(phone = phone)
                )
            )
        )

        RetrofitClient.instance.createPaymentMethod(paymentMethodRequest)
            .enqueue(object : retrofit2.Callback<PaymentMethodResponse> {
                override fun onResponse(
                    call: retrofit2.Call<PaymentMethodResponse>,
                    response: retrofit2.Response<PaymentMethodResponse>
                ) {
                    if (response.isSuccessful) {
                        val paymentMethodId = response.body()?.data?.id
                        Log.d("PayWithGCash", "Payment Method created: id=$paymentMethodId")
                        if (paymentMethodId != null) {
                            callback(paymentMethodId)
                        } else {
                            Log.e("PayWithGCash", "Payment Method creation failed: Missing paymentMethodId")
                        }
                    } else {
                        Log.e("PayWithGCash", "Payment Method API Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<PaymentMethodResponse>, t: Throwable) {
                    Log.e("PayWithGCash", "Payment Method API Failure: ${t.message}", t)
                }
            })
    }

    private fun attachPayment(clientKey: String, paymentMethodId: String, callback: (String) -> Unit) {
        Log.d("PayWithGCash", "Attaching Payment Method: $paymentMethodId to Intent with clientKey: $clientKey")

        val attachPaymentRequest = AttachPaymentRequest(
            data = AttachPaymentData(
                attributes = AttachPaymentAttributes(
                    payment_method = null,  // Set to null when using source
                    client_key = clientKey,
                    amount = 10000, // Ensure this matches the original amount
                    currency = "PHP",
                    source = AttachSource(
                        id = paymentMethodId,
                        type = "source" // Correct type for the source
                    )
                )
            )
        )


        RetrofitClient.instance.attachPayment(attachPaymentRequest)
            .enqueue(object : retrofit2.Callback<PaymentResponse> {
                override fun onResponse(
                    call: retrofit2.Call<PaymentResponse>,
                    response: retrofit2.Response<PaymentResponse>
                ) {
                    if (response.isSuccessful) {
                        val checkoutUrl = response.body()?.data?.attributes?.redirect?.checkout_url
                        Log.d("PayWithGCash", "Payment attached successfully. Checkout URL: $checkoutUrl")
                        if (checkoutUrl != null) {
                            callback(checkoutUrl)
                        } else {
                            Log.e("PayWithGCash", "Attach Payment failed: Missing checkoutUrl")
                            showToast("Attach Payment failed: Missing checkout URL")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("PayWithGCash", "Attach Payment API Error: ${response.code()} - $errorBody")
                        showToast("Attach Payment API Error: $errorBody")
                    }
                }

                override fun onFailure(call: retrofit2.Call<PaymentResponse>, t: Throwable) {
                    Log.e("PayWithGCash", "Attach Payment API Failure: ${t.message}", t)
                    showToast("Network error: ${t.message}")
                }
            })
    }

    private fun openCheckoutPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}
