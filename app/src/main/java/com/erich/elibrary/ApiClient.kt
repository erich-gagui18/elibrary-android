package com.erich.elibrary

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Emulator: 10.0.2.2 is your PC's localhost.
    // Real phone on the same Wi-Fi: use your PC's LAN IP (e.g. http://192.168.1.10:8000/api/)
    // and start Laravel with: php artisan serve --host=0.0.0.0
    const val BASE_URL = "http://10.0.2.2:8000/api/"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            // Member token wins; otherwise send the guest token. The backend enforces the limits.
            val request = chain.request().newBuilder().header("Accept", "application/json")
            val token = Session.token
            val guest = Session.guestToken
            if (token != null) request.header("Authorization", "Bearer $token")
            else if (guest != null) request.header("X-Guest-Token", guest)
            chain.proceed(request.build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}
