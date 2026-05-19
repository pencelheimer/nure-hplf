package com.social.app.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.social.app.data.api.ApiClient
import com.social.app.data.model.SearchResult
import com.social.app.timeAgo
import com.social.app.ui.AppViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val _results = MutableStateFlow<SearchResult?>(null)
    val results: StateFlow<SearchResult?> = _results.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun search(q: String) {
        if (q.isBlank()) return
        viewModelScope.launch {
            _loading.value = true
            runCatching { ApiClient.apiService.search(q) }.onSuccess { _results.value = it }
            _loading.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(appViewModel: AppViewModel, navController: NavHostController) {
    val searchViewModel: SearchViewModel = viewModel()
    val results by searchViewModel.results.collectAsState()
    val loading by searchViewModel.loading.collectAsState()
    var query by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search users or posts…") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { searchViewModel.search(query) }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        }

        if (loading) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        results?.let { r ->
            item { Text("Users", style = MaterialTheme.typography.titleMedium) }

            if (r.users.isEmpty()) {
                item {
                    Text(
                        "No users found.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            items(r.users, key = { "u${it.id}" }) { u ->
                ListItem(
                    headlineContent = { Text(u.username) },
                    modifier = Modifier.clickable { navController.navigate("profile/${u.id}") }
                )
                HorizontalDivider()
            }

            item { Text("Posts", style = MaterialTheme.typography.titleMedium) }

            if (r.posts.isEmpty()) {
                item {
                    Text(
                        "No posts found.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            items(r.posts, key = { "p${it.id}" }) { post ->
                PostCard(
                    post = post,
                    isMe = false,
                    onUserClick = { navController.navigate("profile/${post.user_id}") },
                    onPostClick = { navController.navigate("post/${post.id}") }
                )
            }
        }
    }
}
