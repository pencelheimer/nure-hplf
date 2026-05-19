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
import com.social.app.data.model.ContentBody
import com.social.app.data.model.PostDetail
import com.social.app.timeAgo
import com.social.app.ui.AppViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostViewModel(private val postId: Int) : ViewModel() {
    private val _post = MutableStateFlow<PostDetail?>(null)
    val post: StateFlow<PostDetail?> = _post.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { ApiClient.apiService.getPost(postId) }.onSuccess { _post.value = it }
        }
    }

    fun addComment(content: String, onDone: () -> Unit) {
        viewModelScope.launch {
            runCatching { ApiClient.apiService.addComment(postId, ContentBody(content)) }
                .onSuccess { comments -> _post.value = _post.value?.copy(comments = comments); onDone() }
        }
    }

    class Factory(private val postId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = PostViewModel(postId) as T
    }
}

@Composable
fun PostScreen(postId: Int, appViewModel: AppViewModel, navController: NavHostController) {
    val me by appViewModel.user.collectAsState()
    val postViewModel: PostViewModel = viewModel(factory = PostViewModel.Factory(postId))
    val post by postViewModel.post.collectAsState()
    var commentText by remember { mutableStateOf("") }

    if (post == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val p = post!!

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { navController.navigate("profile/${p.user_id}") },
                            contentPadding = PaddingValues(0.dp)
                        ) { Text(p.username, style = MaterialTheme.typography.labelMedium) }
                        if (p.user_id == me?.id) {
                            Text(
                                " (you)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            timeAgo(p.created_at),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(p.content, modifier = Modifier.padding(vertical = 6.dp))
                }
            }
        }

        if (p.comments.isEmpty()) {
            item { Text("No comments yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }

        items(p.comments, key = { it.id }) { c ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { navController.navigate("profile/${c.user_id}") },
                            contentPadding = PaddingValues(0.dp)
                        ) { Text(c.username, style = MaterialTheme.typography.labelSmall) }
                        Spacer(Modifier.weight(1f))
                        Text(
                            timeAgo(c.created_at),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(c.content)
                }
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Add a comment…") },
                    modifier = Modifier.weight(1f),
                    minLines = 1
                )
                FilledTonalButton(
                    onClick = {
                        if (commentText.isBlank()) return@FilledTonalButton
                        postViewModel.addComment(commentText) { commentText = "" }
                    },
                    modifier = Modifier.height(56.dp)
                ) { Text("Reply") }
            }
        }
    }
}
