package com.example.kotlinactivites.NavBar

import AttachPaymentAttributes
import AttachPaymentData
import AttachPaymentRequest
import PaymentDetails
import PaymentIntentAttributes
import PaymentIntentData
import PaymentIntentRequest
import PaymentMethodAttributes
import PaymentMethodData
import PaymentMethodRequest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.kotlinactivites.Network.PaymentIntentResponse
import com.example.kotlinactivites.Network.PaymentMethodResponse
import com.example.kotlinactivites.Network.PaymentResponse
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
            initiateGcashPayment(10000, "09171234567")
        }

        return view
    }

    private fun initiateGcashPayment(amount: Int, phone: String) {
        // Step 1: Create a Payment Intent
        createPaymentIntent(amount) { clientKey, paymentIntentId ->
            // Step 2: Create a Payment Method for GCash
            createPaymentMethod(phone) { paymentMethodId ->
                // Step 3: Attach Payment Method to Payment Intent
                attachPayment(clientKey, paymentMethodId) { checkoutUrl ->
                    // Step 4: Redirect the User to the GCash Checkout URL
                    openCheckoutPage(checkoutUrl)
                }
            }
        }
    }

    private fun createPaymentIntent(amount: Int, callback: (String, String) -> Unit) {
        // Use the Retrofit API to create a payment intent
        val paymentIntentRequest = PaymentIntentRequest(
            data = PaymentIntentData(
                attributes = PaymentIntentAttributes(
                    amount = amount,
                    currency = "PHP"
                )
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
                            showToast("Error: Missing client key or ID")
                        }
                    } else {
                        showToast("Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<PaymentIntentResponse>, t: Throwable) {
                    showToast("Payment Intent API Failure: ${t.message}")
                }
            })
    }

    private fun createPaymentMethod(phone: String, callback: (String) -> Unit) {
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
                        if (paymentMethodId != null) {
                            callback(paymentMethodId)
                        } else {
                            showToast("Error: Missing Payment Method ID")
                        }
                    } else {
                        showToast("Payment Method Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<PaymentMethodResponse>, t: Throwable) {
                    showToast("Payment Method API Failure: ${t.message}")
                }
            })
    }

    private fun attachPayment(clientKey: String, paymentMethodId: String, callback: (String) -> Unit) {
        val attachPaymentRequest = AttachPaymentRequest(
            data = AttachPaymentData(
                attributes = AttachPaymentAttributes(
                    payment_method = paymentMethodId,
                    client_key = clientKey
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
                        if (checkoutUrl != null) {
                            callback(checkoutUrl)
                        } else {
                            showToast("Error: Missing Checkout URL")
                        }
                    } else {
                        showToast("Attach Payment Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<PaymentResponse>, t: Throwable) {
                    showToast("Attach Payment API Failure: ${t.message}")
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
