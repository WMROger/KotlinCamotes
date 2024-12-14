package com.example.kotlinactivites.Network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface PayMongoService {
    @Headers("Content-Type: application/json")
    @POST("payment_intents")
    fun createPaymentIntent(@Body request: PaymentIntentRequest): Call<PaymentIntentResponse>

    @Headers("Content-Type: application/json")
    @POST("payment_methods")
    fun createPaymentMethod(@Body request: PaymentMethodRequest): Call<PaymentMethodResponse>

    @Headers("Content-Type: application/json")
    @POST("payments")
    fun attachPayment(@Body request: AttachPaymentRequest): Call<PaymentResponse>

    @Headers("Content-Type: application/json")
    @POST("sources")
    fun createSource(@Body request: SourceRequest): Call<SourceResponse>


}
