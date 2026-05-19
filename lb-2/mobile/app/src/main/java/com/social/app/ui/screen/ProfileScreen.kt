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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.social.app.data.api.ApiClient
import com.social.app.data.model.Friendship
import com.social.app.data.model.UserProfile
import com.social.app.timeAgo
import com.social.app.ui.AppViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val userId: Int) : ViewModel() {
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { ApiClient.apiService.getUser(userId) }
                .onSuccess { _profile.value = it }
                .onFailure { _error.value = "User not found" }
        }
    }

    fun sendFriendRequest(meId: Int) {
        viewModelScope.launch {
            runCatching { ApiClient.apiService.sendFriendRequest(userId) }
                .onSuccess { _profile.value = _profile.value?.copy(friendship = Friendship("pending", meId)) }
                .onFailure { _error.value = it.message ?: "Error" }
        }
    }

    class Factory(private val userId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = ProfileViewModel(userId) as T
    }
}

@Composable
fun ProfileScreen(userId: Int, appViewModel: AppViewModel, navController: NavHostController) {
    val me by appViewModel.user.collectAsState()
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory(userId))
    val profile by profileViewModel.profile.collectAsState()
    val error by profileViewModel.error.collectAsState()
    val isMe = userId == me?.id

    if (error.isNotEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        return
    }

    if (profile == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val p = profile!!

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(p.username, style = MaterialTheme.typography.headlineMedium)
                    if (!isMe) {
                        when {
                            p.friendship == null ->
                                FilledTonalButton(onClick = { profileViewModel.sendFriendRequest(me?.id ?: 0) }) {
                                    Text("Add friend")
                                }
                            p.friendship.status == "pending" ->
                                AssistChip(onClick = {}, label = { Text("Request pending") })
                            else ->
                                AssistChip(onClick = {}, label = { Text("Friends") })
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Joined ${timeAgo(p.created_at)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!isMe) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { navController.navigate("messages/$userId") }) {
                        Text("Message")
                    }
                }
            }
        }

        if (p.posts.isEmpty()) {
            item { Text("No posts yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }

        items(p.posts, key = { it.id }) { post ->
            PostCard(
                post = post,
                isMe = isMe,
                onUserClick = {},
                onPostClick = { navController.navigate("post/${post.id}") }
            )
        }
    }
}
