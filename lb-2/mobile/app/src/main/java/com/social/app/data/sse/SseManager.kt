package com.social.app.data.sse

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

object SseManager {
    fun connect(
        client: OkHttpClient,
        url: String,
        onEvent: (type: String, data: String) -> Unit
    ): EventSource {
        // NOTE(pencelheimer): infinite read timeout so the SSE connection stays open
        val sseClient = client.newBuilder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder().url(url).build()

        return EventSources.createFactory(sseClient).newEventSource(
            request,
            object : EventSourceListener() {
                override fun onEvent(source: EventSource, id: String?, type: String?, data: String) {
                    if (type != null) onEvent(type, data)
                }

                override fun onFailure(source: EventSource, t: Throwable?, response: Response?) {}
            }
        )
    }
}
