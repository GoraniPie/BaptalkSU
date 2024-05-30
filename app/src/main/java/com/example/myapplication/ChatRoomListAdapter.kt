package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DateFormat
import java.util.Date

class ChatRoomAdapter(
    private val chatRooms: List<ChatRoom>,
    private val onItemClick: (ChatRoom) -> Unit
) : RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder>() {

    inner class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomName: TextView = itemView.findViewById(R.id.roomName)
        val lastMessage: TextView = itemView.findViewById(R.id.lastMessage)
        val lastMessageSender: TextView = itemView.findViewById(R.id.lastMessageSender)
        val lastMessageTime: TextView = itemView.findViewById(R.id.lastMessageTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_room, parent, false)
        return ChatRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        val chatRoom = chatRooms[position]
        holder.roomName.text = chatRoom.roomName
        holder.lastMessage.text = chatRoom.lastMessage

        val database = FirebaseFirestore.getInstance()
        if (chatRoom.lastMessageSender != "") {
            val userDocRef = database.collection("user").document(chatRoom.lastMessageSender)
            Log.i("userDocRef까진 괜찮음", "ㅇㅇ")
            userDocRef.get().addOnSuccessListener { document ->
                Log.i("db 가져오기 성공", "ㅇㅇ")
                holder.lastMessageSender.text = document.getString("name")
            }
        }



        holder.lastMessageTime.text = DateFormat.getDateTimeInstance().format(Date(chatRoom.lastMessageTime))

        holder.itemView.setOnClickListener {
            onItemClick(chatRoom)
        }
    }

    override fun getItemCount() = chatRooms.size
}