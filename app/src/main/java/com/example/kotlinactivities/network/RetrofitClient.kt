package com.example.kotlinactivities.network

import android.util.Base64
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://api.paymongo.com/v1/"
    private const val API_KEY = "sk_test_fqEgjZa69ADU9GB5MoaJkoUz" // Replace with your PayMongo secret key

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val basicAuth = "Basic ${Base64.encodeToString("$API_KEY:".toByteArray(), Base64.NO_WRAP)}"

            val newRequest = originalRequest.newBuilder()
                .header("Authorization", basicAuth)
                .build()

            chain.proceed(newRequest)
        }
        .build()


    val instance: PayMongoService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PayMongoService::class.java)
    }
}
