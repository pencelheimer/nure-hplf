package com.social.app.data.api

import com.social.app.BuildConfig
import com.social.app.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private val BASE_URL = "http://${BuildConfig.API_HOST}:${BuildConfig.API_PORT}/"

    private var sessionManager: SessionManager? = null

    lateinit var okHttpClient: OkHttpClient
        private set

    lateinit var apiService: ApiService
        private set

    fun initialize(sm: SessionManager) {
        sessionManager = sm

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val token = sessionManager?.getToken()
            val request = if (token != null) {
                original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                original
            }
            chain.proceed(request)
        }

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }
}
