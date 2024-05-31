package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DateFormat

class MessageAdapter(private val context: Context, private val messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageLayout: LinearLayout = itemView.findViewById(R.id.ly_Message)
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val messageSender: TextView = itemView.findViewById(R.id.messageSender)
        val messageTime: TextView = itemView.findViewById(R.id.messageTime)
        val profileButton: ImageView = itemView.findViewById(R.id.iv_ChatProfileImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        // 프로필 조회하기
        holder.profileButton.setOnClickListener {
            val intent = Intent(context, ViewProfile::class.java)
            intent.putExtra("uid", message.senderId)
            context.startActivity(intent)
        }


        holder.messageText.text = message.message
        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser?.uid == message.senderId) {
            holder.messageSender.text = "나"
            holder.messageLayout.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.light_skyblue))
        } else {
            val firestore = FirebaseFirestore.getInstance()
            val user = auth.currentUser
            if (user != null) {
                val userDocRef = firestore.collection("user").document(message.senderId)
                userDocRef.get().addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        holder.messageSender.text = document.getString("name")
                    } else {
                        // 탈퇴한 사용자 (회원 정보 db에 없음)
                        holder.messageSender.text = "탈퇴한 사용자"
                    }
                }.addOnFailureListener {
                    holder.messageSender.text = "알 수 없음"
                }
            }
        }
        holder.messageTime.text = DateFormat.getDateTimeInstance().format(message.timestamp)
    }

    override fun getItemCount() = messages.size
}