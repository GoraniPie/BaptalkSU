package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        val buttonExitChat: ImageButton = findViewById(R.id.ibt_ExitChat)

        val tvChatTitle = findViewById<TextView>(R.id.tv_ChatRoomTitle)
        val auth = FirebaseAuth.getInstance()

        firestore = FirebaseFirestore.getInstance()

        messageAdapter = MessageAdapter(this, messages)
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
                    senderId = auth.currentUser?.uid ?: "Anonymous",
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

        buttonExitChat.setOnClickListener {
            Log.i("채팅방 나가기 눌러짐", "ㅇㅇ")
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setTitle("채팅방 나가기")
            dialogBuilder.setMessage("채팅방을 나가시겠습니까?")
            dialogBuilder.setNegativeButton("닫기") { dialog, which ->
                dialog.dismiss()
            }
            dialogBuilder.setPositiveButton("나가기") { dialog, which ->
                database.child("chatRooms").child(roomId).child("creatorId").get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.i("creatorId", task.result.toString())
                            // 채팅방 생성자(=모집글 작성자)가 나가면 방 파괴
                            if (task.result.value == auth.currentUser?.uid) {
                                database.child("chatRooms").child(roomId).removeValue()
                                    .addOnCompleteListener {
                                        firestore.collection("recruitment").document(roomId)
                                            .delete()
                                            .addOnCompleteListener {
                                                finish()
                                            }
                                    }.addOnFailureListener {
                                        Toast.makeText(this, "채팅방 나가기 실패", Toast.LENGTH_LONG).show()
                                    }
                            } else {
                                // 일반 사용자 나가기
                                database.child("chatRooms").child(roomId).child("users")
                                    .child(auth.currentUser?.uid ?: "")
                                    .setValue(false).addOnCompleteListener {
                                        Log.i("db상 채팅방 나가짐 완료", "ㅇㅇ")
                                        val firestore = FirebaseFirestore.getInstance()
                                        val chatRoomData =
                                            firestore.collection("recruitment").document(roomId)
                                        chatRoomData.get().addOnSuccessListener { document ->
                                            val headcountCurrent =
                                                document.getLong("headcount_current") ?: 0
                                            // 현재 인원 업데이트
                                            val update = hashMapOf<String, Any>(
                                                "headcount_current" to (headcountCurrent - 1),
                                            )
                                            firestore.collection("recruitment").document(roomId)
                                                .update(update)
                                            finish()
                                        }.addOnFailureListener {
                                            Log.i("db상 채팅방 나가짐 실패", "ㅠㅠ")
                                            Toast.makeText(this, "채팅방 나가기 실패", Toast.LENGTH_LONG)
                                                .show()
                                        }
                                    }
                            }
                        }
                        else Log.i("채팅방 생성자 찾기실패", "ㅇㅇ")
                    }

            }
            dialogBuilder.create().show()

            // TODO: 모집글 생성자는 참가자 강제퇴장시킬 수 있음.

        }

    }
}