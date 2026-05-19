package com.social.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.social.app.BuildConfig
import com.social.app.SessionManager
import com.social.app.data.api.ApiClient
import com.social.app.data.model.LoginRequest
import com.social.app.data.model.User
import com.social.app.data.sse.SseManager
import com.social.app.parseApiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.sse.EventSource

data class LiveEvent(
    val type: String,
    val data: String,
    val seq: Int
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _liveEvent = MutableStateFlow<LiveEvent?>(null)
    val liveEvent: StateFlow<LiveEvent?> = _liveEvent.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private var eventSource: EventSource? = null
    private var eventSeq = 0

    init {
        ApiClient.initialize(sessionManager)
        val token = sessionManager.getToken()
        if (token != null) {
            viewModelScope.launch {
                try {
                    val me = ApiClient.apiService.getMe()
                    _user.value = me
                    refreshUnread()
                    connectSse(token)
                } catch (e: Exception) {
                    sessionManager.clearToken()
                }
            }
        }
    }

    fun login(username: String, password: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { ApiClient.apiService.login(LoginRequest(username, password)) }
                .onSuccess { resp -> onLogin(resp.user, resp.token) }
                .onFailure { onError(parseApiError(it)) }
        }
    }

    fun register(username: String, password: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { ApiClient.apiService.register(LoginRequest(username, password)) }
                .onSuccess { resp -> onLogin(resp.user, resp.token) }
                .onFailure { onError(parseApiError(it)) }
        }
    }

    fun onLogin(user: User, token: String) {
        sessionManager.saveToken(token)
        _user.value = user
        refreshUnread()
        connectSse(token)
    }

    fun logout() {
        viewModelScope.launch {
            try { ApiClient.apiService.logout() } catch (_: Exception) {}
            sessionManager.clearToken()
            _user.value = null
            cancelSse()
            _unreadCount.value = 0
        }
    }

    fun refreshUnread() {
        viewModelScope.launch {
            try {
                val resp = ApiClient.apiService.getUnreadCount()
                _unreadCount.value = resp.count
            } catch (_: Exception) {}
        }
    }

    fun connectSse(token: String) {
        cancelSse()
        val url = "http://${BuildConfig.API_HOST}:${BuildConfig.API_PORT}/api/events?token=$token"
        eventSource = SseManager.connect(ApiClient.okHttpClient, url) { type, data ->
            eventSeq++
            _liveEvent.value = LiveEvent(type = type, data = data, seq = eventSeq)
            if (type == "new_message") {
                _unreadCount.value = _unreadCount.value + 1
            }
        }
    }

    private fun cancelSse() {
        eventSource?.cancel()
        eventSource = null
    }

    override fun onCleared() {
        super.onCleared()
        cancelSse()
    }
}
