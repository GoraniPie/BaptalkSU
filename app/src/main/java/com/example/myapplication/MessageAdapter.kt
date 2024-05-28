package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import java.text.DateFormat

class MessageAdapter(private val messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageLayout: LinearLayout = itemView.findViewById(R.id.ly_Message)
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val messageSender: TextView = itemView.findViewById(R.id.messageSender)
        val messageTime: TextView = itemView.findViewById(R.id.messageTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.message
        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser?.uid == message.senderId) {
            holder.messageSender.text = "나"
            holder.messageLayout.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.light_skyblue))
        }
        else {

            // TODO: UID말고, cloud firestore에 검색해서 계정정보의 이름으로 보이게 설정.
            holder.messageSender.text = message.senderId
        }
        holder.messageTime.text = DateFormat.getDateTimeInstance().format(message.timestamp)
    }

    override fun getItemCount() = messages.size
}