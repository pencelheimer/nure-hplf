package com.social.app

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun getToken(): String? = prefs.getString("token", null)
    fun saveToken(token: String) { prefs.edit().putString("token", token).apply() }
    fun clearToken() { prefs.edit().remove("token").apply() }
}
