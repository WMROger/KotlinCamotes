package com.example.kotlinactivites.Network

// Source Request
data class SourceRequest(
    val data: SourceData
)

data class SourceData(
    val attributes: SourceAttributes
)

data class SourceAttributes(
    val amount: Int,
    val currency: String = "PHP",
    val type: String = "gcash",
    val redirect: Redirect
)

data class Redirect(
    val success: String,
    val failed: String
)

// Source Response
data class SourceResponse(
    val data: SourceResponseData
)

data class SourceResponseData(
    val id: String,
    val attributes: SourceAttributesResponse
)

data class SourceAttributesResponse(
    val status: String,
    val redirect: SourceRedirect
)

data class SourceRedirect(
    val checkout_url: String
)
