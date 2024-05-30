package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var firestore: FirebaseFirestore
    private lateinit var messageAdapter: MessageAdapter
    private var messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat)

        val recyclerView: RecyclerView = findViewById(R.id.rcv_ChatMessages)
        val editTextMessage: EditText = findViewById(R.id.et_TextMessage)
        val buttonSend: ImageView = findViewById(R.id.img_SendMessage)

        val tvChatTitle = findViewById<TextView>(R.id.tv_ChatRoomTitle)

        firestore = FirebaseFirestore.getInstance()

        messageAdapter = MessageAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageAdapter

        val roomId = intent.getStringExtra("roomId") ?: return
        database = FirebaseDatabase.getInstance().reference

        // 채팅방 제목 설정
        database.child("chatRooms").child(roomId).child("roomName").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("가져오기 성공", "예쓰")
                    Log.i("task 값", task.result.toString())
                    val roomName = task.result?.getValue(String::class.java)
                    if (roomName != null && roomName.isNotEmpty()) {
                        tvChatTitle.text = roomName
                    }
                    else {
                        tvChatTitle.text = "채팅방"
                    }
                }
                else {
                    tvChatTitle.text = "채팅방"
                }
            }

        // 메시지 전송하면 채팅 새로고침
        database.child("messages").child(roomId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("addValueEvent, onDataChange 발동됨", "ㅇㅇㅇㅇ")
                messages.clear()
                for (data in snapshot.children) {
                    val message = data.getValue(Message::class.java)
                    if (message != null) {
                        messages.add(message)
                    }
                }
                messageAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("db 에러", "메시지 전송 실패")
            }
        })

        // 전송버튼 누를 때
        buttonSend.setOnClickListener {
            val messageText = editTextMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val messageId = database.child("messages").child(roomId).push().key ?: return@setOnClickListener
                val message = Message(
                    messageId = messageId,
                    senderId = FirebaseAuth.getInstance().currentUser?.uid ?: "Anonymous",
                    message = messageText,
                    timestamp = System.currentTimeMillis()
                )
                database.child("messages").child(roomId).child(messageId).setValue(message)
                editTextMessage.text.clear()
                // 마지막 메시지
                database.child("chatRooms").child(roomId).child("lastMessage").setValue(message.message)
                database.child("chatRooms").child(roomId).child("lastMessageSender").setValue(message.senderId)
                database.child("chatRooms").child(roomId).child("lastMessageTime").setValue(message.timestamp)
            }
        }

        // 채팅방 닫기
        val closeChat = findViewById<ImageView>(R.id.img_Closechat)
        closeChat.setOnClickListener {
            finish()
        }
    }
}