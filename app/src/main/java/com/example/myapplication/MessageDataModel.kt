package com.example.myapplication

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
)