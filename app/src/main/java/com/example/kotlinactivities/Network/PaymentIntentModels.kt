package com.example.kotlinactivities.Network

data class PaymentIntentRequest(
    val data: PaymentIntentData
)

data class PaymentIntentData(
    val attributes: PaymentIntentAttributes
)

data class PaymentIntentAttributes(
    val amount: Int,  // Amount in cents (e.g., PHP 100.00 = 10000)
    val currency: String = "PHP",
    val payment_method_allowed: List<String> = listOf("gcash"),
    val capture_type: String = "automatic"
)

data class PaymentIntentResponse(
    val data: PaymentIntentResponseData
)

data class PaymentIntentResponseData(
    val id: String,
    val attributes: PaymentIntentAttributesResponse
)

data class PaymentIntentAttributesResponse(
    val amount: Int,
    val currency: String,
    val client_key: String,
    val status: String,
    val created_at: Long
)
