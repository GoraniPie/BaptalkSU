package com.example.myapplication

import android.app.TimePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.vane.badwordfiltering.BadWordFiltering
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecruitModify : AppCompatActivity() {
    private var parentListener: ParentActivityListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.modify_detail_recruit)
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            for (key in extras.keySet()) {
                val value = extras.get(key)
                Log.d("IntentExtra", "$key : $value")
            }
        } else {
            Log.d("IntentExtra", "No extras found")
        }
        val uploaderId = intent.getStringExtra("uploader_id")
        val uploaderName = intent.getStringExtra("uploader_name")
        val uploaderAge = intent.getLongExtra("uploader_age", -1)
        val uploaderMajor = intent.getStringExtra("uploader_major")

        val postId = intent.getStringExtra("post_id")
        val baptime = intent.getLongExtra("baptime", 0)
        val title = intent.getStringExtra("title")
        val place = intent.getStringExtra("place")
        val content = intent.getStringExtra("content")
        val keywordAgeMin = intent.getIntExtra("keyword_age_min", -1)
        val keywordAgeMax = intent.getIntExtra("keyword_age_max", -1)
        val keywordSex = intent.getStringExtra("keyword_sex")
        val keywordMajor = intent.getStringExtra("keyword_major")
        val keywordMBTI = intent.getStringExtra("keyword_mbti")

        val tvName = findViewById<TextView>(R.id.tv_ModifyName)
        val tvMajor = findViewById<TextView>(R.id.tv_ModifyMajor)
        val tvAge = findViewById<TextView>(R.id.tv_ModifyAge)
        val etTitle = findViewById<EditText>(R.id.et_ModifyTitle)
        val etPlace = findViewById<EditText>(R.id.et_ModifyPlace)
        val tvTime = findViewById<TextView>(R.id.tv_ModifyTime)
        val etContent = findViewById<EditText>(R.id.et_ModifyContent)
        val tvKeywordSex = findViewById<TextView>(R.id.tv_ModifyKeywordSex)
        val tvKeywordAge = findViewById<TextView>(R.id.tv_ModifyKeywordAge)
        val tvKeywordMBTI = findViewById<TextView>(R.id.tv_ModifyKeywordMBTI)
        val tvKeywordMajor = findViewById<TextView>(R.id.tv_ModifyKeywordMajor)

        // baptime 시간 설정
        val baptimeHour: Int = ((baptime / (1000 * 60 * 60)) % 24).toInt()
        val baptimeMinute: Int = ((baptime / (1000 * 60)) % 60).toInt()
        val tv_InputTime = tvTime
        val calendar = Calendar.getInstance()
        var date: Date = calendar.time
        var hour: Int = baptimeHour
        var minute: Int = baptimeMinute
        var selectedTime: String
        tv_InputTime.setOnClickListener {
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                selectedTime = "${selectedHour}시 ${selectedMinute}분"
                tv_InputTime.text = selectedTime
                // 선택한 시간으로 Date 객체 생성
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                date = calendar.time
            }, hour, minute, true)
            timePickerDialog.show()
        }


        tvName.text = uploaderName
        tvMajor.text = uploaderMajor
        tvAge.text = "나이 : " + uploaderAge.toString() + "세"

        etTitle.setText(title)
        etPlace.setText(place)

        tvTime.setText("${baptimeHour}시 ${baptimeMinute}분")

        etContent.setText(content)
        if (keywordSex == "") {
            tvKeywordSex.text = "성별제한 없음"
        } else {
            tvKeywordSex.text = keywordSex
        }


        if (keywordAgeMin == -1) {
            if (keywordAgeMax == -1) {
                tvKeywordAge.text = "나이제한 없음"
            }
            else {
                tvKeywordAge.text = "~ ${keywordAgeMax}세"
            }
        }
        else {
            if (keywordAgeMax == -1) {
                tvKeywordAge.text = "${keywordAgeMin} ~ 세"
            }
            else {
                tvKeywordAge.text = "${keywordAgeMin} ~ ${keywordAgeMax}세"
            }
        }

        if (keywordMBTI == "") {
            tvKeywordMBTI.text = "#MBTI 비공개"
        } else {
            tvKeywordMBTI.text = keywordMBTI
        }

        if (keywordMajor == "") {
            tvKeywordMajor.text = "학과제한 없음"
        } else {
            tvKeywordMajor.text = keywordMajor
        }


        // 나가기 버튼
        val btClose = findViewById<ImageButton>(R.id.ibt_CloseModifyRecruit)
        btClose.setOnClickListener {
            finish()
        }

        // 삭제하기
        val deleteRecruit = findViewById<Button>(R.id.bt_DeleteRecruit)
        deleteRecruit.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setTitle("")
            dialogBuilder.setMessage("삭제하시겠습니까? 채팅방도 함께 삭제됩니다.")
            // 다이얼로그 팝업
            dialogBuilder.setNegativeButton("취소") { dialog, which ->
                dialog.dismiss()
            }
            dialogBuilder.setPositiveButton("삭제하기") { dialog, _ ->
                val db = FirebaseFirestore.getInstance()
                db.collection("recruitment")
                    .document(postId?:"")
                    .delete()
                    .addOnSuccessListener {
                        // 성공적으로 삭제되었을 때의 처리ㅁㄴ
                        setResult(RESULT_OK)
                        val realtime = FirebaseDatabase.getInstance().reference
                        realtime.child("chatRooms").child(postId?:"").removeValue()
                            .addOnSuccessListener {
                                Log.i("글삭->채팅방삭제완", "ㅇㅇ")
                                parentListener?.onCloseParentActivity()

                            }
                        finish()
                    }
                    .addOnFailureListener { e ->
                        popUpDialog("삭제에 실패했습니다.")
                    }
            }
            dialogBuilder.create().show()
        }

        // 변경사항 저장하기
        val saveRecruit = findViewById<Button>(R.id.bt_SubmitModification)
        saveRecruit.setOnClickListener {
            val finalTitle = etTitle.text.toString().trim()
            val finalPlace = etPlace.text.toString().trim()
            val finalTime = date
            val finalTimeText = tvTime.text.toString()
            val finalContent = etContent.text.toString().trim()
            val filter = BadWordFiltering()

            // 빈 제목
            if (finalTitle == "") {
                popUpDialog("제목을 입력해주세요.")
            }
            // 빈 장소
            else if (finalPlace == "") {
                popUpDialog("장소를 입력해주세요.")
            }
            // 빈 시간
            else if (finalTimeText == "") {
                popUpDialog("시간을 입력해주세요.")
            }
            // 빈 내용
            else if (finalContent.trim() == "") {
                popUpDialog("내용을 입력해주세요.")
            }
            // 제목 욕설
            else if (filter.check(finalTitle)) {
                popUpDialog("제목에 부적절한 표현이 포함돼있습니다.")
            }
            // 장소 욕설
            else if (filter.check(finalPlace)) {
                popUpDialog("장소에 부적절한 표현이 포함돼있습니다.")
            }
            // 내용 욕설
            else if (filter.check(finalContent)) {
                popUpDialog("내용에 부적절한 표현이 포함돼있습니다.")
            }
            else {
                val updates = hashMapOf<String, Any>(
                    "last_modified" to FieldValue.serverTimestamp(), // 현재 서버 시간으로 업데이트
                    "title" to finalTitle,
                    "place" to finalPlace,
                    "content" to finalContent,
                    "title_content" to "$finalTitle $finalPlace $finalContent",
                    "baptime" to Timestamp(date),
                )
                val db = FirebaseFirestore.getInstance()
                val docRef = db.collection("recruitment").document(postId.toString())
                docRef.update(updates)
                    .addOnSuccessListener {
                        finish()
                    }
                    .addOnFailureListener {
                        popUpDialog("글 등록에 실패했습니다.")
                    }
            }
        }


    }

    public fun popUpDialog(msg: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("")
        dialogBuilder.setMessage(msg)
        // 다이얼로그 팝업
        dialogBuilder.setNegativeButton("닫기") { dialog, which ->
            dialog.dismiss()
        }
        dialogBuilder.create().show()
    }
}