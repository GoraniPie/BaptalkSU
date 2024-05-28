package com.example.myapplication

import com.google.firebase.Timestamp

data class ChatRoom(
    val roomId: String = "",
    val roomName: String = "",
    val lastMessage: String = "",
    val lastMessageSender: String = "",
    val lastMessageTime: Timestamp
)