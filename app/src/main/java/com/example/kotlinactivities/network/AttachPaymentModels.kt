package com.example.kotlinactivities.network

data class AttachPaymentRequest(
    val data: AttachPaymentData
)

data class AttachPaymentData(
    val attributes: AttachPaymentAttributes
)

data class AttachPaymentAttributes(
    val payment_method: String?,  // Optional if using source
    val client_key: String,       // Client key from Payment Intent
    val amount: Int,              // Amount in cents
    val currency: String = "PHP", // Currency
    val source: AttachSource      // Add source for GCash
)

data class AttachSource(
    val id: String,     // Source ID (payment method ID)
    val type: String    // Source type (e.g., "gcash")
)

data class PaymentResponse(
    val data: PaymentResponseData
)

data class PaymentResponseData(
    val id: String,
    val attributes: PaymentAttributesResponse
)

data class PaymentAttributesResponse(
    val status: String,
    val redirect: PaymentRedirect
)

data class PaymentRedirect(
    val checkout_url: String
)
