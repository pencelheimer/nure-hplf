package com.social.app.data.model

data class User(val id: Int, val username: String, val created_at: Long)

data class Post(
    val id: Int,
    val user_id: Int,
    val username: String,
    val content: String,
    val created_at: Long,
    val comment_count: Int? = null
)

data class Comment(
    val id: Int,
    val post_id: Int,
    val user_id: Int,
    val username: String,
    val content: String,
    val created_at: Long
)

data class PostDetail(
    val id: Int,
    val user_id: Int,
    val username: String,
    val content: String,
    val created_at: Long,
    val comments: List<Comment>
)

data class FriendRequest(
    val id: Int,
    val user_id: Int,
    val username: String,
    val created_at: Long
)

data class Friendship(val status: String, val requester_id: Int)

data class UserProfile(
    val id: Int,
    val username: String,
    val created_at: Long,
    val posts: List<Post>,
    val friendship: Friendship? = null
)

data class Message(
    val id: Int,
    val from_id: Int,
    val to_id: Int,
    val from_username: String,
    val content: String,
    val created_at: Long,
    val is_read: Int
)

data class Conversation(
    val other_id: Int,
    val other_username: String,
    val last_message_at: Long
)

data class SearchResult(val users: List<User>, val posts: List<Post>)

data class AuthResponse(val token: String, val user: User)
data class UnreadCountResponse(val count: Int)
data class OkResponse(val ok: Boolean)

// Request bodies
data class LoginRequest(val username: String, val password: String)
data class ContentBody(val content: String)
