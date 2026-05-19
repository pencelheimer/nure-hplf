package com.pz3.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pz3.app.data.api.WeatherApi
import com.pz3.app.data.model.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class WeatherState {
    object Idle : WeatherState()
    object Loading : WeatherState()
    data class Success(val data: WeatherResponse) : WeatherState()
    data class Error(val message: String) : WeatherState()
}

class WeatherViewModel : ViewModel() {
    private val _state = MutableStateFlow<WeatherState>(WeatherState.Idle)
    val state: StateFlow<WeatherState> = _state

    fun load(city: String) {
        viewModelScope.launch {
            _state.value = WeatherState.Loading
            try {
                val response = WeatherApi.service.getWeather(city)
                _state.value = WeatherState.Success(response)
            } catch (e: Exception) {
                _state.value = WeatherState.Error(e.message ?: "Невідома помилка")
            }
        }
    }
}

@Composable
fun WeatherScreen(vm: WeatherViewModel = viewModel()) {
    var city by remember { mutableStateOf("Kharkiv") }
    val state by vm.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Погода",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Місто") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { vm.load(city) }) {
                Text("Пошук")
            }
        }

        Spacer(Modifier.height(24.dp))

        when (val s = state) {
            is WeatherState.Idle -> Text(
                "Введіть назву міста та натисніть пошук",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            is WeatherState.Loading -> CircularProgressIndicator()
            is WeatherState.Error -> Text(
                "Помилка: ${s.message}",
                color = MaterialTheme.colorScheme.error
            )
            is WeatherState.Success -> {
                val current = s.data.current_condition.firstOrNull() ?: return@Column
                val area = s.data.nearest_area.firstOrNull()
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = area?.region?.firstOrNull()?.value ?: city,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "${current.temp_C}°C — ${current.weatherDesc.firstOrNull()?.value ?: ""}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        HorizontalDivider()
                        Text("Відчувається як: ${current.FeelsLikeC}°C")
                        Text("Вологість: ${current.humidity}%")
                        Text("Вітер: ${current.windspeedKmph} км/год")
                    }
                }
            }
        }
    }
}
