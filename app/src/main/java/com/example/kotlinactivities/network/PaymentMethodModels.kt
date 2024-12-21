package com.example.kotlinactivities.network

data class PaymentMethodRequest(
    val data: PaymentMethodData
)

data class PaymentMethodData(
    val attributes: PaymentMethodAttributes
)

data class PaymentMethodAttributes(
    val type: String = "gcash",
    val details: PaymentDetails
)

data class PaymentDetails(
    val phone: String  // User's phone number
)

data class PaymentMethodResponse(
    val data: PaymentMethodResponseData
)

data class PaymentMethodResponseData(
    val id: String,
    val attributes: PaymentMethodAttributesResponse
)

data class PaymentMethodAttributesResponse(
    val type: String,
    val created_at: Long
)
