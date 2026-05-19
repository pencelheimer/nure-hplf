package com.pz3.app.data.model

data class WeatherResponse(
    val current_condition: List<CurrentCondition>,
    val nearest_area: List<NearestArea>
)

data class CurrentCondition(
    val temp_C: String,
    val FeelsLikeC: String,
    val humidity: String,
    val windspeedKmph: String,
    val weatherDesc: List<WeatherDesc>
)

data class WeatherDesc(val value: String)

data class NearestArea(
    val region: List<ValueWrapper>,
    val country: List<ValueWrapper>
)

data class ValueWrapper(val value: String)
