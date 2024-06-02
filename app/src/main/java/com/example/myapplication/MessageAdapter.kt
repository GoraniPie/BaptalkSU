package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MessageAdapter(private val context: Context, private val messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var messageConstraint: ConstraintLayout = itemView.findViewById(R.id.ly_MessageConstraint)
        val senderInfoSection: LinearLayout = itemView.findViewById(R.id.ly_SenderInfo)
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

        // ConstraintSet 초기화
        val constraintSet = ConstraintSet()
        constraintSet.clone(holder.messageConstraint)

        // 내가 보낸 메시지
        if (auth.currentUser?.uid == message.senderId) {
            holder.messageLayout.setBackgroundResource(R.drawable.sender_message_background)
            holder.senderInfoSection.visibility = View.GONE

            // 오른쪽 정렬
            constraintSet.constrainPercentWidth(R.id.ly_Message, 0.8f)
            constraintSet.connect(R.id.ly_Message, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            constraintSet.clear(R.id.ly_Message, ConstraintSet.START)
        }
        // 상대가 보낸 메시지
        else {
            holder.messageLayout.setBackgroundResource(R.drawable.receiver_message_background)
            holder.senderInfoSection.visibility = View.VISIBLE

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

            // 왼쪽 정렬
            constraintSet.constrainPercentWidth(R.id.ly_Message, 0.8f)
            constraintSet.connect(R.id.ly_Message, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            constraintSet.clear(R.id.ly_Message, ConstraintSet.END)
        }

        // 적용된 제약조건 설정
        constraintSet.applyTo(holder.messageConstraint)

        val sdf = SimpleDateFormat("MM월 dd일\nHH시 mm분", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = message.timestamp
        holder.messageTime.text = sdf.format(calendar.time)
    }

    override fun getItemCount() = messages.size
}