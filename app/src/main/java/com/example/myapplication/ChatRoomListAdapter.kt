package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DateFormat
import java.util.Date

class ChatRoomAdapter(
    private var chatRooms: List<ChatRoom>,
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
            userDocRef.get().addOnSuccessListener { document ->
                holder.lastMessageSender.text = document.getString("name") + " :"
            }
        } else {
            holder.lastMessageSender.text = "새로운 채팅방입니다."
        }

        holder.lastMessageTime.text = DateFormat.getDateTimeInstance().format(Date(chatRoom.lastMessageTime))

        // 채팅방 제목 설정
        val headcount = database.collection("recruitment").document(chatRoom.roomId)
        headcount.get().addOnSuccessListener { document ->
            val headcountCurrent = document.getLong("headcount_current") ?: 0
            val headcountMax = document.getLong("headcount_max") ?: 0
            holder.roomName.text = "${chatRoom.roomName} ($headcountCurrent / $headcountMax)"
        }.addOnFailureListener {
            holder.roomName.text = "알 수 없음"
        }

        holder.itemView.setOnClickListener {
            onItemClick(chatRoom)
        }
    }

    override fun getItemCount() = chatRooms.size
    fun updateList(newChatRooms: List<ChatRoom>) {
        chatRooms = newChatRooms.sortedByDescending { it.lastMessageTime }
        notifyDataSetChanged()
    }
}