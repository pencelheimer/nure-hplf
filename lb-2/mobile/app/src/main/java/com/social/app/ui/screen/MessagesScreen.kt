package com.social.app.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.google.gson.Gson
import com.social.app.data.api.ApiClient
import com.social.app.data.model.ContentBody
import com.social.app.data.model.Conversation
import com.social.app.data.model.Message
import com.social.app.timeAgo
import com.social.app.ui.AppViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessagesViewModel(private val activeUserId: Int?) : ViewModel() {
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { ApiClient.apiService.getConversations() }.onSuccess { _conversations.value = it }
            if (activeUserId != null) {
                runCatching { ApiClient.apiService.getThread(activeUserId) }.onSuccess { _messages.value = it }
            }
            _loading.value = false
        }
    }

    fun refreshConversations() {
        viewModelScope.launch {
            runCatching { ApiClient.apiService.getConversations() }.onSuccess { _conversations.value = it }
        }
    }

    fun send(content: String, onDone: () -> Unit) {
        if (activeUserId == null) return
        viewModelScope.launch {
            runCatching { ApiClient.apiService.sendMessage(activeUserId, ContentBody(content)) }
                .onSuccess { msg -> _messages.value = _messages.value + msg; onDone() }
        }
    }

    fun onNewMessage(msg: Message) {
        if (activeUserId != null && msg.from_id == activeUserId) {
            _messages.value = _messages.value + msg
        }
        refreshConversations()
    }

    class Factory(private val activeUserId: Int?) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = MessagesViewModel(activeUserId) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(activeUserId: Int?, appViewModel: AppViewModel, navController: NavHostController) {
    val me by appViewModel.user.collectAsState()
    val liveEvent by appViewModel.liveEvent.collectAsState()
    val msgsViewModel: MessagesViewModel = viewModel(factory = MessagesViewModel.Factory(activeUserId))
    val conversations by msgsViewModel.conversations.collectAsState()
    val messages by msgsViewModel.messages.collectAsState()
    val loading by msgsViewModel.loading.collectAsState()
    val gson = remember { Gson() }
    val listState = rememberLazyListState()

    LaunchedEffect(liveEvent) {
        if (liveEvent?.type == "new_message") {
            val msg = gson.fromJson(liveEvent!!.data, Message::class.java)
            msgsViewModel.onNewMessage(msg)
            appViewModel.refreshUnread()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    if (activeUserId == null) {
        // Conversations list
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
            if (conversations.isEmpty()) {
                item {
                    Text(
                        "No conversations yet. Open one from a user's profile.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            items(conversations, key = { it.other_id }) { conv ->
                ListItem(
                    headlineContent = { Text(conv.other_username) },
                    supportingContent = {
                        Text(timeAgo(conv.last_message_at), style = MaterialTheme.typography.bodySmall)
                    },
                    modifier = Modifier.clickable { navController.navigate("messages/${conv.other_id}") }
                )
                HorizontalDivider()
            }
        }
    } else {
        // Message thread
        val activeConv = conversations.find { it.other_id == activeUserId }
        var draft by remember { mutableStateOf("") }

        Column(Modifier.fillMaxSize()) {
            // Simple top bar (no nested Scaffold needed)
            Row(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                TextButton(
                    onClick = { navController.navigate("profile/$activeUserId") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        activeConv?.other_username ?: "User #$activeUserId",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            HorizontalDivider()

            if (loading) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Text("No messages yet. Say hello!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    items(messages, key = { it.id }) { m ->
                        val isMine = m.from_id == me?.id
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
                        ) {
                            Surface(
                                color = if (isMine) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    m.content,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    color = if (isMine) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                timeAgo(m.created_at),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp).imePadding(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text("Type a message…") },
                    modifier = Modifier.weight(1f),
                    minLines = 1
                )
                FilledTonalIconButton(
                    onClick = {
                        if (draft.isBlank()) return@FilledTonalIconButton
                        msgsViewModel.send(draft) { draft = "" }
                    },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}
