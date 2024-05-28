package com.example.myapplication

import com.google.firebase.Timestamp

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val message: String = "",
    val timestamp: Timestamp
)