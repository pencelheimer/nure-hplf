package com.social.app

import retrofit2.HttpException

fun timeAgo(ts: Long): String {
    val secs = System.currentTimeMillis() / 1000 - ts
    return when {
        secs < 60    -> "just now"
        secs < 3600  -> "${secs / 60}m ago"
        secs < 86400 -> "${secs / 3600}h ago"
        else         -> "${secs / 86400}d ago"
    }
}

fun parseApiError(e: Throwable): String {
    if (e is HttpException) {
        return try {
            val body = e.response()?.errorBody()?.string() ?: return e.message()
            val match = Regex("\"error\"\\s*:\\s*\"([^\"]+)\"").find(body)
            match?.groupValues?.get(1) ?: e.message()
        } catch (_: Exception) { e.message() }
    }
    return e.message ?: "Unknown error"
}
