package com.pz3.app.data.api

import com.pz3.app.data.model.WeatherResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherService {
    @GET("{city}")
    suspend fun getWeather(
        @Path("city") city: String,
        @Query("format") format: String = "j1",
        @Query("lang") lang: String = "uk",
    ): WeatherResponse
}

object WeatherApi {
    val service: WeatherService = Retrofit.Builder()
        .baseUrl("https://wttr.in/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherService::class.java)
}
