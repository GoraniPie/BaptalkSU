package com.example.myapplication

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.w3c.dom.Text
import java.util.Date

class RecruitAdapter(private var recruitList: List<RecruitDataModel>, private val context: Context) :
    RecyclerView.Adapter<RecruitAdapter.RecruitViewHolder>() {
    inner class RecruitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.recruitTitle)
        val baptime: TextView = itemView.findViewById(R.id.recruitBaptime)
        val place: TextView = itemView.findViewById(R.id.recruitPlace)
        val uploader: TextView = itemView.findViewById(R.id.recruitUploader)
    }
    private lateinit var postRecruitmentLauncher: ActivityResultLauncher<Intent>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecruitViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recruit, parent, false)
        return RecruitViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecruitViewHolder, position: Int) {
        val recruit = recruitList[position]
        val firestore = FirebaseFirestore.getInstance()
        // 리스트에 유저 이름 표시
        var uploaderName: String = ""
        var uploaderAge: Long = -1
        var uploaderMajor: String = ""
        var hour: String = ""
        var minute: String = ""

        // db에서 데이터 긁어오기
        firestore.collection("user").document(recruit.uploader_id).get().addOnSuccessListener {document->
            if (document.exists()) {
                Log.i("여기서 에러", document.getString("name")?:"" + document.getString("uid")?:"")
                Log.i("recruit정보", recruit.post_id)
                val birthday = document.getTimestamp("birthday")?.toDate()
                val date: Date? = birthday
                val calendar: Calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                calendar.time = date
                val userBirthYear = calendar.get(Calendar.YEAR)

                uploaderAge = (currentYear - userBirthYear).toLong()
                uploaderMajor = document.getString("major")?:""

                uploaderName = document.getString("name")?:"알 수 없음"
                holder.uploader.text = uploaderName
                // 제목, 인원, 식사장소 표시
                holder.title.text = "${recruit.title} (${recruit.headcount_current} / ${recruit.headcount_max})"
                holder.place.text = "식사 장소 : ${recruit.place}"
                // 모집 시간 표시
                val date2 = recruit.baptime?.toDate()
                val calendar2: Calendar = Calendar.getInstance()
                calendar2.time = date2
                hour = calendar2.get(Calendar.HOUR_OF_DAY).toString()
                minute = calendar2.get(Calendar.MINUTE).toString()
                holder.baptime.text = "식사 시간 : ${hour}시 ${minute}분"
            }
            else {
                holder.uploader.text = "탈퇴한 사용자"
            }
        }.addOnFailureListener {
            holder.uploader.text = "탈퇴한 사용자"
        }



        // 모집글 세부조회
        holder.itemView.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            var documentExists = true
            db.collection("recruitment")
                .document(recruit.post_id)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Log.i("document아이디1", recruit.post_id)
                        documentExists = true
                    } else {
                        Log.i("document아이디2", recruit.post_id)
                        documentExists = false
                        Log.i("documentExist값", documentExists.toString())

                    }
                    if (!documentExists) {
                        popUpDialog(context, "존재하지 않는 문서입니다.")
                    }
                    else {
                    val dialog = Dialog(holder.itemView.context)
                    dialog.setContentView(R.layout.detail_recruit)

                    val titleTextView = dialog.findViewById<TextView>(R.id.tv_Title)
                    val placeTextView = dialog.findViewById<TextView>(R.id.tv_Place)
                    val baptimeTextView = dialog.findViewById<TextView>(R.id.tv_Time)
                    val tv_Age = dialog.findViewById<TextView>(R.id.tv_Age)
                    val tv_Content = dialog.findViewById<TextView>(R.id.tv_Content)

                    val tv_keywordMajor = dialog.findViewById<TextView>(R.id.tv_KeywordMajor)
                    val tv_keywordSex = dialog.findViewById<TextView>(R.id.tv_KeywordSex)
                    val tv_keywordMBTI = dialog.findViewById<TextView>(R.id.tv_KeywordMBTI)
                    val tv_keywordAge = dialog.findViewById<TextView>(R.id.tv_KeywordAge)
                    val tv_uploaderName = dialog.findViewById<TextView>(R.id.tv_Name)

                    // 닫기 버튼
                    val btClosePopup = dialog.findViewById<ImageButton>(R.id.ibt_ClosePopup)
                    btClosePopup.setOnClickListener {
                        Log.i("팝업버튼 닫기", "클릭확인됨")
                        dialog.dismiss()
                    }

                    // 아이템 데이터 설정
                    titleTextView.text = recruit.title
                    placeTextView.text = "식사 장소 : ${recruit.place}"
                    baptimeTextView.text = "식사 시간 : ${hour}시 ${minute}분"
                    tv_uploaderName.text = "모집자 : " + uploaderName
                    tv_Age.text = "모집자 나이 : ${uploaderAge}세"


                    //mbti 태그
                    if (recruit.keyword_mbti == "") {
                        tv_keywordMBTI.text = "#MBTI 비공개"
                    } else {
                        tv_keywordMBTI.text = "#" + recruit.keyword_mbti
                    }

                    // 성별 태그
                    if (recruit.keyword_sex == "") {
                        tv_keywordSex.text = "성별 제한 없음"
                    } else {
                        tv_keywordSex.text = "#" + recruit.keyword_sex + "만"
                    }

                    // 확과 설정
                    if (recruit.keyword_major == "") {
                        tv_keywordMajor.text = "학과 제한 없음"
                    } else {
                        tv_keywordMajor.text = recruit.keyword_major
                    }
                    // 나이 제한
                    if (recruit.keyword_age_max == -1) {
                        if (recruit.keyword_age_min == -1) {
                            tv_keywordAge.text = "나이 제한 없음"
                        } else {
                            tv_keywordAge.text = "${recruit.keyword_age_min} ~ 세만"
                        }
                    } else {
                        if (recruit.keyword_age_min == -1) {
                            tv_keywordAge.text = "~ ${recruit.keyword_age_max}세만"
                        } else {
                            tv_keywordAge.text =
                                "${recruit.keyword_age_min} ~ ${recruit.keyword_age_max}세만"
                        }
                    }

                    tv_Content.text = recruit.content

                    val viewProfile = dialog.findViewById<ImageView>(R.id.iv_UploaderProfileImage)
                    viewProfile.setOnClickListener {
                        val intent = Intent(context, ViewProfile::class.java)
                        intent.putExtra("uid", recruit.uploader_id)
                        context.startActivity(intent) // 여기서 context를 사용
                    }


                    // 수정하기
                    val btModify = dialog.findViewById<ImageView>(R.id.iv_ModifyRecruit)
                    val auth = FirebaseAuth.getInstance()
                    if (recruit.uploader_id == auth.currentUser?.uid) {
                        btModify.visibility = View.VISIBLE
                    }
                    btModify.setOnClickListener {
                        val intent = Intent(context, RecruitModify::class.java)
                        intent.putExtra("uploader_id", recruit.uploader_id)
                        intent.putExtra("uploader_name", uploaderName)
                        intent.putExtra("uploader_major", uploaderMajor)
                        intent.putExtra("uploader_age", uploaderAge)
                        intent.putExtra("baptime", recruit.baptime?.toDate()?.time ?: 0)
                        intent.putExtra("post_id", recruit.post_id)
                        intent.putExtra("title", recruit.title)
                        intent.putExtra("place", recruit.place)
                        intent.putExtra("content", recruit.content)
                        intent.putExtra("keyword_age_min", recruit.keyword_age_min)
                        intent.putExtra("keyword_age_max", recruit.keyword_age_max)
                        intent.putExtra("keyword_major", recruit.keyword_major)
                        intent.putExtra("keyword_mbti", recruit.keyword_mbti)
                        intent.putExtra("keyword_sex", recruit.keyword_sex)
                        checkRecruitExist(recruit.post_id).addOnSuccessListener {exists->
                            if (exists == true) context.startActivity(intent)
                            else {
                                popUpDialog(context, "존재하지 않는 문서입니다.")
                                dialog.dismiss()
                            }
                        }

                    }


                    // 참여하기  
                    val btJoin = dialog.findViewById<Button>(R.id.bt_EnterRecruit)
                        val currentUser = FirebaseAuth.getInstance().currentUser
                    btJoin.setOnClickListener {
                        val db = FirebaseFirestore.getInstance()
                        var documentExists = true
                        db.collection("recruitment")
                            .document(recruit.post_id)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    Log.i("document아이디1", recruit.post_id)
                                    documentExists = true
                                } else {
                                    Log.i("document아이디2", recruit.post_id)
                                    documentExists = false
                                    Log.i("documentExist값", documentExists.toString())

                                }
                                if (!documentExists) {
                                    popUpDialog(context, "존재하지 않는 문서입니다.")
                                }
                                else {
                                    val database: DatabaseReference = FirebaseDatabase.getInstance().reference
                                    val firestore = FirebaseFirestore.getInstance()
                                    // 최대 참여자 수에 도달한 경우 참여 거절. 아니라면 참여
                                    firestore.collection("recruitment").document(recruit.post_id).get()
                                        .addOnSuccessListener { document ->
                                            val currentHeadcount = document.getLong("headcount_current") ?: 0
                                            val maxHeadcount = document.getLong("headcount_max") ?: 0

                                            val databaseReference = FirebaseDatabase.getInstance().reference

                                            val chatRoomRef =
                                                databaseReference.child("chatRooms").child(recruit.post_id)
                                                    .child("users").child(currentUser?.uid?:"")
                                            var isRegistered: Boolean? = false

                                            chatRoomRef.get().addOnSuccessListener {
                                                // 이미 참가중인가 체크
                                                if (it.getValue(Boolean::class.java) == true) {
                                                    popUpDialog(context, "이미 참가한 모집입니다.")
                                                }
                                                // 꽉 찬 모집인가 체크
                                                else if (currentHeadcount >= maxHeadcount) {
                                                    popUpDialog(context, "이미 꽉 찬 모집입니다.")
                                                }
                                                else {
                                                    checkRecruitExist(recruit.post_id).addOnSuccessListener { exists ->
                                                        if (exists) {
                                                            // 문서가 존재하는 경우
                                                            // 키워드 검사
                                                            val auth = FirebaseAuth.getInstance()
                                                            val user = auth.currentUser
                                                            var userAge: Long = -1
                                                            var userMajor: String = ""
                                                            var userSex: String = ""
                                                            if (user != null) {
                                                                firestore.collection("user").document(user.uid).get()
                                                                    .addOnSuccessListener { document ->
                                                                        userAge = document.getLong("age") ?: -1
                                                                        userMajor = document.getString("major") ?: ""
                                                                        userSex = document.getString("sex") ?: ""
                                                                        if (user != null) {
                                                                            if (
                                                                                !((document.getString("uploader_id") == (user.uid?:""))
                                                                                        ||
                                                                                        // 조건을 만족하는 글만 보이기
                                                                                        ((document.getLong("keyword_age_max")?:-1L == -1L || document.getLong("keyword_age_max")?:-1L >= userAge)
                                                                                                && (document.getLong("keyword_age_min")?:-1L == -1L || document.getLong("keyword_age_min")?:-1L <= userAge)
                                                                                                && (document.getString("keyword_sex")?:"" == "" || document.getString("keyword_sex")?:"" == userSex)
                                                                                                && (document.getString("keyword_major")?:"" == "" || document.getString("keyword_major")?:"" == userMajor)))
                                                                            ) {
                                                                                popUpDialog(context, "참가할 수 없는 모집입니다.")
                                                                            } else {
                                                                                database.child("chatRooms").child(recruit.post_id)
                                                                                    .child("users").child(currentUser?.uid?:"")
                                                                                    .setValue(true)
                                                                                    .addOnCompleteListener { task ->
                                                                                        if (task.isSuccessful) {
                                                                                            Log.i("채팅방 참여 성공", "ㅇㅇ")
                                                                                            val firestore =
                                                                                                FirebaseFirestore.getInstance()
                                                                                            val chatRoomData =
                                                                                                firestore.collection("recruitment")
                                                                                                    .document(recruit.post_id)
                                                                                            chatRoomData.get()
                                                                                                .addOnSuccessListener { document ->
                                                                                                    val headcountCurrent =
                                                                                                        document.getLong("headcount_current")
                                                                                                            ?: 0
                                                                                                    // 현재 인원 업데이트
                                                                                                    val update =
                                                                                                        hashMapOf<String, Any>(
                                                                                                            "headcount_current" to (headcountCurrent + 1),
                                                                                                        )
                                                                                                    firestore.collection("recruitment")
                                                                                                        .document(recruit.post_id)
                                                                                                        .update(update)
                                                                                                }
                                                                                        } else {
                                                                                            popUpDialog(context, "참여에 실패했습니다.")
                                                                                        }
                                                                                    }
                                                                            }
                                                                        }
                                                                    }
                                                            }

                                                        } else {
                                                            // 문서가 존재하지 않는 경우
                                                            popUpDialog(context, "존재하지 않는 문서입니다.")
                                                        }
                                                    }


                                                }
                                            }
                                                .addOnFailureListener {                        // 채팅방의 사용자 목록에 현재 사용자 추가
                                                    database.child("chatRooms").child(recruit.post_id)
                                                        .child("users").child(currentUser?.uid?:"").setValue(true)
                                                        .addOnCompleteListener { task ->
                                                            if (task.isSuccessful) {
                                                                Log.i("채팅방 참여 성공", "ㅇㅇ")
                                                                val firestore = FirebaseFirestore.getInstance()
                                                                val chatRoomData =
                                                                    firestore.collection("recruitment")
                                                                        .document(recruit.post_id)
                                                                chatRoomData.get()
                                                                    .addOnSuccessListener { document ->
                                                                        val headcountCurrent =
                                                                            document.getLong("headcount_current")
                                                                                ?: 0
                                                                        // 현재 인원 업데이트
                                                                        val update = hashMapOf<String, Any>(
                                                                            "headcount_current" to (headcountCurrent + 1),
                                                                        )
                                                                        firestore.collection("recruitment")
                                                                            .document(recruit.post_id)
                                                                            .update(update)
                                                                    }
                                                            } else {
                                                                popUpDialog(context, "참여에 실패했습니다.")
                                                            }
                                                        }
                                                }
                                        }
                                }
                            }
                        Log.i("참여하기", "클릭됨")
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser == null) {
                            return@setOnClickListener
                        }

                        
                    }
                        dialog.show()
                    }
                }.addOnFailureListener {
                    Log.i("document아이디3", recruit.post_id)
                    documentExists = false
                }

        }
    }

    override fun getItemCount() = recruitList.size

    fun addItems(newList: List<RecruitDataModel>) {
        recruitList += newList
    }

    fun checkRecruitExist(postId: String): Task<Boolean> {
        val db = FirebaseFirestore.getInstance()

        // 문서 가져오기 작업을 수행하는 Task
        val documentTask = db.collection("recruitment")
            .document(postId)
            .get()

        // 문서 가져오기 작업이 완료되면 해당 문서가 존재하는지 여부를 반환하는 Task
        return documentTask.continueWith { task ->
            if (task.isSuccessful) {
                val document: DocumentSnapshot? = task.result
                document?.exists() ?: false
            } else {
                false
            }
        }
    }

    fun popUpDialog(context: Context, msg: String) {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setTitle("")
        dialogBuilder.setMessage(msg)
        // 다이얼로그 팝업
        dialogBuilder.setNegativeButton("닫기") { dialog, _ ->
            dialog.dismiss()
        }
        dialogBuilder.create().show()
    }

    fun updateList(newList: List<RecruitDataModel>) {
        recruitList = newList
        notifyDataSetChanged()
    }
}