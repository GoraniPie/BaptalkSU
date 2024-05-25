package com.example.myapplication

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.vane.badwordfiltering.BadWordFiltering
import java.util.Date
import java.util.UUID

class PostRecruitment : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.post_recruit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 뒤로가기 버튼
        val goBackBtn = findViewById<Button>(R.id.bt_GoBack1)
        goBackBtn.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }

        // time picker 밥타임 시간
        val tv_InputTime = findViewById<TextView>(R.id.tv_BapTime)
        val calendar = Calendar.getInstance()
        var hour = -1
        var minute = -1
        var selectedTime: String
        tv_InputTime.setOnClickListener {

            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                selectedTime = "${selectedHour}시 ${selectedMinute}분"
                tv_InputTime.text = selectedTime
            }, hour, minute, true)

            timePickerDialog.show()
        }

        var selectedMajor: String = ""
        var selectedMBTI: String = ""
        var selectedSex: String = ""

        // 장소 자동완성
        val placeTextView: AutoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.et_Place)
        // 자동 완성 리스트
        val places = arrayOf(
            "토리코코로",
            "맛차이나",
            "마녀떡볶이",
            "몽키파스타",
            "맘스터치",
            "스마일닭갈비",
            "창부리또",
            "쌀국수공방",
            "청와삼대",
            "담터추어탕",
            "담터쭈꾸미",
            "샌두",
            "바글바글베이커리",
            "린스테이블",
            "수내닭꼬치",
            "별미가",
            "스마일하우스",
            "파인하우스",
            "홍원",
            "다람이임자탕",
            "쇼쿠오쿠",
            "요기요거트",
            "최고집해물찜칼국수",
            "후라이드참잘하는집",
            "교내",
            "곽만근갈비탕",
            "도그라운지",
            "세상만사감자탕",
            "신룽푸마라탕",
            "편의점",
            "카페공강",
            "매머드커피",

        )
        // 어댑터 설정
        val autocompadapter: ArrayAdapter<String> = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            places
        )
        placeTextView.setAdapter(autocompadapter)

        // 전공 키워드
        val spinnerDepartments: Spinner = findViewById(R.id.sp_KeywordMajor)
        ArrayAdapter.createFromResource(
            this,
            R.array.keyword_department,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // 드롭다운 설정
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // 어댑터를 스피너에 연결
            spinnerDepartments.adapter = adapter
        }
        spinnerDepartments.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            // 선택 학과에 저장
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedMajor = parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedMajor = ""
            }
        }

        // mbti 키워드
        val spinnerMBTI: Spinner = findViewById(R.id.sp_KeywordMBTI)
        ArrayAdapter.createFromResource(
            this,
            R.array.keyword_mbti,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // 드롭다운 설정
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // 어댑터를 스피너에 연결
            spinnerMBTI.adapter = adapter
        }
        spinnerMBTI.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            // 선택 mbti에 저장
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedMBTI = parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedMBTI = ""
            }
        }

        // 성별 키워드
        val spinnerSex: Spinner = findViewById(R.id.sp_KeywordSex)
        ArrayAdapter.createFromResource(
            this,
            R.array.keyword_sex,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // 드롭다운 설정
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // 어댑터를 스피너에 연결
            spinnerSex.adapter = adapter
        }
        spinnerSex.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            // 선택 성별에 저장
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedSex = parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedSex = ""
            }
        }

        // 작성하기 버튼
        val postRecruit = findViewById<Button>(R.id.bt_PostRecruit)
        postRecruit.setOnClickListener {
            // 폼 데이터 가져오기
            val title = findViewById<EditText>(R.id.et_Title).text.trim().toString()
            val place = findViewById<EditText>(R.id.et_Place).text.trim().toString()
            val content = findViewById<EditText>(R.id.et_MultiLine).text.trim().toString()
            var keywordMajor = selectedMajor
            var keywordMBTI = selectedMBTI
            var keywordSex = selectedSex

            val inputAgeMin = findViewById<EditText>(R.id.et_KeywordAgeMin).text.trim().toString()
            val keywordAgeMin: Int
            if (inputAgeMin.isEmpty()) {
                keywordAgeMin = -1
            }
            else {
                keywordAgeMin = inputAgeMin.toInt()
            }

            val inputAgeMax = findViewById<EditText>(R.id.et_KeywordAgeMax).text.trim().toString()
            val keywordAgeMax: Int
            if (inputAgeMin.isEmpty()) {
                keywordAgeMax = -1
            }
            else {
                keywordAgeMax = inputAgeMax.toInt()
            }
            val bapTimeHour = hour
            val bapTimeMinute = minute
            val bapTime: Timestamp = setTimestamp(hour, minute)

            // 폼 무결성 확인
            val filter: BadWordFiltering = BadWordFiltering() // 욕설 필터링
            // 빈 제목
            if (title.isEmpty()) {
                popUpDialog("제목을 입력해주세요.")
            }
            // 시간 미입력
            else if (hour == -1 || minute == -1) {
                popUpDialog("시간을 입력해주세요.")
            }
            // 빈 장소
            else if (place.isEmpty()) {
                popUpDialog("장소를 입력해주세요.")
            }
            // 빈 내용
            else if (content.isEmpty()) {
                popUpDialog("내용을 입력해주세요.")
            }
            // 제목 욕설
            else if (filter.check(title)) {
                popUpDialog("제목에 부적절한 표현이 포함돼있습니다.")
            }
            // 장소 욕설
            else if (filter.check(place)) {
                popUpDialog("장소에 부적절한 표현이 포함돼있습니다.")
            }
            // 내용 욕설
            else if (filter.check(content)) {
                popUpDialog("내용에 부적절한 표현이 포함돼있습니다.")
            }
            // 최소나이 최대나이 무결성
            else if (keywordAgeMax != -1 && keywordAgeMin != -1 && keywordAgeMin > keywordAgeMax) {
                popUpDialog("잘못된 나이제한입니다.")
            }
            else {
                // db에 저장
                auth = FirebaseAuth.getInstance()
                db = FirebaseFirestore.getInstance()
                val user = auth.currentUser

                if (keywordSex == "#성별") keywordSex = ""
                if (keywordMBTI == "#MBTI") keywordMBTI = ""
                if (keywordMajor == "#학과") keywordMajor = ""

                val postID = UUID.randomUUID().toString()
                val currentTimestamp = FieldValue.serverTimestamp()
                val recruitmentData = hashMapOf(
                    "baptime" to bapTime,
                    "created_at" to currentTimestamp,
                    "modified_at" to currentTimestamp,
                    "headcount_current" to 1,
                    "headcount_max" to 2,
                    "keyword_age_min" to keywordAgeMin,
                    "keyword_age_max" to keywordAgeMax,
                    "keyword_major" to keywordMajor,
                    "keyword_mbti" to keywordMBTI,
                    "keyword_sex" to keywordSex,
                    "place" to place,
                    "post_id" to postID,
                    "title" to title,
                    "uploader_id" to user?.uid,
                    "content" to content,
                )
                db.collection("recruitment").document(postID)
                    .set(recruitmentData)

                // 닫기, TODO : 작성 후 조회 리스트 자동 갱신
                setResult(Activity.RESULT_OK)
                finish()

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
    private fun setTimestamp(hour: Int, minute: Int): Timestamp {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val timestamp = Timestamp(calendar.time)
        return timestamp
    }
}