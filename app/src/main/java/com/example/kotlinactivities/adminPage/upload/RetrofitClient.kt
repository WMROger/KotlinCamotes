package com.example.kotlinactivities.adminPage.upload

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://waveaway.scarlet2.io/"

    val instance: UploadApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UploadApi::class.java)
    }
}
