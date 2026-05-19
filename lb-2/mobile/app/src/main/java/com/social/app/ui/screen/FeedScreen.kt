package com.social.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.social.app.data.api.ApiClient
import com.social.app.data.model.ContentBody
import com.social.app.data.model.FriendRequest
import com.social.app.data.model.Post
import com.social.app.timeAgo
import com.social.app.ui.AppViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _requests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val requests: StateFlow<List<FriendRequest>> = _requests.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { ApiClient.apiService.getFeed() }.onSuccess { _posts.value = it }
            runCatching { ApiClient.apiService.getFriendRequests() }.onSuccess { _requests.value = it }
            _loading.value = false
        }
    }

    fun createPost(content: String, onDone: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { ApiClient.apiService.createPost(ContentBody(content)) }
                .onSuccess { post ->
                    if (_posts.value.none { it.id == post.id })
                        _posts.value = listOf(post.copy(comment_count = 0)) + _posts.value
                    onDone()
                }
                .onFailure { onError(it.message ?: "Error") }
        }
    }

    fun acceptRequest(req: FriendRequest) {
        viewModelScope.launch {
            runCatching { ApiClient.apiService.acceptFriendRequest(req.id) }
                .onSuccess {
                    _requests.value = _requests.value.filter { it.id != req.id }
                    runCatching { ApiClient.apiService.getFeed() }.onSuccess { _posts.value = it }
                }
        }
    }

    fun onNewPost(post: Post) {
        if (_posts.value.none { it.id == post.id })
            _posts.value = listOf(post.copy(comment_count = 0)) + _posts.value
    }

    fun onNewFriendRequest(req: FriendRequest) {
        if (_requests.value.none { it.id == req.id })
            _requests.value = listOf(req) + _requests.value
    }
}

@Composable
fun FeedScreen(appViewModel: AppViewModel, navController: NavHostController) {
    val me by appViewModel.user.collectAsState()
    val liveEvent by appViewModel.liveEvent.collectAsState()
    val feedViewModel: FeedViewModel = viewModel()
    val posts by feedViewModel.posts.collectAsState()
    val requests by feedViewModel.requests.collectAsState()
    val loading by feedViewModel.loading.collectAsState()
    val gson = remember { Gson() }
    var content by remember { mutableStateOf("") }
    var postError by remember { mutableStateOf("") }

    LaunchedEffect(liveEvent) {
        when (liveEvent?.type) {
            "new_post" -> feedViewModel.onNewPost(gson.fromJson(liveEvent!!.data, Post::class.java))
            "new_friend_request" -> feedViewModel.onNewFriendRequest(gson.fromJson(liveEvent!!.data, FriendRequest::class.java))
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (requests.isNotEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Friend requests", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp))
                        requests.forEach { req ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = { navController.navigate("profile/${req.user_id}") },
                                    contentPadding = PaddingValues(0.dp)
                                ) { Text(req.username) }
                                Spacer(Modifier.weight(1f))
                                FilledTonalButton(onClick = { feedViewModel.acceptRequest(req) }) { Text("Accept") }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card {
                Column(Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = { Text("What's on your mind?") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    if (postError.isNotEmpty()) {
                        Text(postError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            if (content.isBlank()) return@Button
                            feedViewModel.createPost(
                                content,
                                onDone = { content = ""; postError = "" },
                                onError = { postError = it }
                            )
                        }) { Text("Post") }
                    }
                }
            }
        }

        if (posts.isEmpty()) {
            item {
                Text(
                    "No posts yet. Find and add some friends to see their posts here.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(posts, key = { it.id }) { post ->
            PostCard(
                post = post,
                isMe = post.user_id == me?.id,
                onUserClick = { navController.navigate("profile/${post.user_id}") },
                onPostClick = { navController.navigate("post/${post.id}") }
            )
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    isMe: Boolean,
    onUserClick: () -> Unit,
    onPostClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onUserClick, contentPadding = PaddingValues(0.dp)) {
                    Text(post.username, style = MaterialTheme.typography.labelMedium)
                }
                if (isMe) {
                    Text(
                        " (you)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    timeAgo(post.created_at),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(post.content, modifier = Modifier.padding(vertical = 6.dp))
            TextButton(onClick = onPostClick, contentPadding = PaddingValues(0.dp)) {
                val count = post.comment_count ?: 0
                Text(
                    "$count ${if (count == 1) "comment" else "comments"}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
