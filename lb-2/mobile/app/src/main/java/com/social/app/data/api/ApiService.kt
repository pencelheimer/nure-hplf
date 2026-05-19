package com.social.app.data.api

import com.social.app.data.model.*
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body body: LoginRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("api/auth/logout")
    suspend fun logout(): OkResponse

    @GET("api/auth/me")
    suspend fun getMe(): User

    @GET("api/posts")
    suspend fun getFeed(): List<Post>

    @POST("api/posts")
    suspend fun createPost(@Body body: ContentBody): Post

    @GET("api/posts/{id}")
    suspend fun getPost(@Path("id") id: Int): PostDetail

    @POST("api/posts/{id}/comments")
    suspend fun addComment(@Path("id") id: Int, @Body body: ContentBody): List<Comment>

    @GET("api/users/me/friend-requests")
    suspend fun getFriendRequests(): List<FriendRequest>

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") id: Int): UserProfile

    @POST("api/users/{id}/friend-request")
    suspend fun sendFriendRequest(@Path("id") id: Int): OkResponse

    @POST("api/users/friend-requests/{id}/accept")
    suspend fun acceptFriendRequest(@Path("id") id: Int): OkResponse

    @GET("api/messages")
    suspend fun getConversations(): List<Conversation>

    @GET("api/messages/unread-count")
    suspend fun getUnreadCount(): UnreadCountResponse

    @GET("api/messages/{userId}")
    suspend fun getThread(@Path("userId") userId: Int): List<Message>

    @POST("api/messages/{userId}")
    suspend fun sendMessage(@Path("userId") userId: Int, @Body body: ContentBody): Message

    @GET("api/search")
    suspend fun search(@Query("q") q: String): SearchResult
}
