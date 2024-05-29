package com.example.myapplication

import android.app.Dialog
import android.icu.util.Calendar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RecruitAdapter(private var recruitList: List<RecruitDataModel>) :
    RecyclerView.Adapter<RecruitAdapter.RecruitViewHolder>() {

    inner class RecruitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.recruitTitle)
        val baptime: TextView = itemView.findViewById(R.id.recruitBaptime)
        val place: TextView = itemView.findViewById(R.id.recruitPlace)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecruitViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recruit, parent, false)
        return RecruitViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecruitViewHolder, position: Int) {
        val recruit = recruitList[position]
        holder.title.text = recruit.title
        holder.place.text = "식사 장소 : ${recruit.place}"

        val date = recruit.baptime?.toDate()
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = date
        val hour = calendar.get(Calendar.HOUR_OF_DAY).toString()
        val minute = calendar.get(Calendar.MINUTE).toString()
        holder.baptime.text = "식사 시간 : ${hour}시 ${minute}분"

        holder.itemView.setOnClickListener {
            val dialog = Dialog(holder.itemView.context)
            dialog.setContentView(R.layout.detail_recruit)

            val titleTextView = dialog.findViewById<TextView>(R.id.tv_Title)
            val placeTextView = dialog.findViewById<TextView>(R.id.tv_Place)
            val baptimeTextView = dialog.findViewById<TextView>(R.id.tv_Time)
            val keywordMajor = dialog.findViewById<TextView>(R.id.tv_KeywordMajor)
            //val keywordSex = dialog.findViewById<TextView>(R.id.tv_KeywordSex)
            val keywordMBTI = dialog.findViewById<TextView>(R.id.tv_KeywordMBTI)

            // 닫기 버튼
            val btClosePopup = dialog.findViewById<ImageButton>(R.id.ibt_ClosePopup)
            btClosePopup.setOnClickListener {
                Log.i("팝업버튼 닫기", "클릭확인됨")
                dialog.dismiss()
            }

            // 아이템 데이터를 설정
            titleTextView.text = recruit.title
            placeTextView.text = "식사 장소 : ${recruit.place}"
            baptimeTextView.text = "식사 시간 : ${hour}시 ${minute}분"

            val btJoin = dialog.findViewById<Button>(R.id.bt_EnterRecruit)
            btJoin.setOnClickListener {
                Log.i("참여하기", "클릭됨")
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    return@setOnClickListener
                }

                // 채팅방의 사용자 목록에 현재 사용자 추가
                val database: DatabaseReference = FirebaseDatabase.getInstance().reference

                database.child("chatRooms").child(recruit.post_id).child("users").child(currentUser.uid).setValue(true)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.i("채팅방 참여 성공", "ㅇㅇ")
                        }
                        else {
                            Log.e("채팅방 참여 실패", "ㅇㅇ")
                        }
                    }
            }

            dialog.show()
        }

    }

    override fun getItemCount() = recruitList.size

    fun updateList(newList: List<RecruitDataModel>) {
        recruitList = newList
        notifyDataSetChanged()
    }
}