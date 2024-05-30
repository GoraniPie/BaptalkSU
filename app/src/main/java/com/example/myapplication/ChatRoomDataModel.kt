package com.example.myapplication

import com.google.firebase.Timestamp
import kotlin.collections.Map

data class ChatRoom(
    val roomId: String = "",
    val roomName: String = "",
    val creatorId: String = "",
    val users: Map<String, Boolean> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageSender: String = "",
    val lastMessageTime: Long = 0L,
)