package com.example.kotlinactivites.Network

// Payment Intent Request and Response
data class PaymentIntentRequest(
    val data: PaymentIntentData
)

data class PaymentIntentData(
    val attributes: PaymentIntentAttributes
)

data class PaymentIntentAttributes(
    val amount: Int,
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

// Payment Method Request and Response
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
    val phone: String
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

// Attach Payment Request and Response
data class AttachPaymentRequest(
    val data: AttachPaymentData
)

data class AttachPaymentData(
    val attributes: AttachPaymentAttributes
)

data class AttachPaymentAttributes(
    val payment_method: String,
    val client_key: String
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
