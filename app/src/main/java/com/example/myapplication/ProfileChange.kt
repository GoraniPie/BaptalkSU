package com.example.myapplication

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.vane.badwordfiltering.BadWordFiltering
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileChange : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_modify)

        // 버튼 연결
        val etName = findViewById<EditText>(R.id.et_NewName)
        val tvStudentId = findViewById<TextView>(R.id.tv_NewStudentId)
        val tvEmail = findViewById<TextView>(R.id.tv_NewEmail)

        val tvBirthday = findViewById<TextView>(R.id.tv_NewBirthday)
        val rdgSex = findViewById<RadioGroup>(R.id.rdg_NewSex)
        val etGrade = findViewById<EditText>(R.id.et_NewGrade)
        val etPofileMsg = findViewById<EditText>(R.id.et_NewProfileMessage)

        // mbti 키워드
        var selectedMBTI = ""
        val spinnerMBTI = findViewById<Spinner>(R.id.et_NewMBTI)
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

        // 학과리스트 드롭다운 내용 연결
        val spinnerDepartments: Spinner = findViewById(R.id.sp_NewMajor)
        ArrayAdapter.createFromResource(
            this,
            R.array.departments_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // 드롭다운 설정
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // 어댑터를 스피너에 연결
            spinnerDepartments.adapter = adapter
        }
        var selectedMajor: String = ""
        spinnerDepartments.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                // 선택 학과에 저장
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    selectedMajor = parent.getItemAtPosition(position) as String
                }
                override fun onNothingSelected(parent: AdapterView<*>) {
                    selectedMajor = ""
                }
            }


        // 생일 캘린더
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        var selectedYear: Int = 2000
        var selectedMonth: Int = 0
        var selectedDay: Int = 1



        // 데이터 불러오기
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            val uid: String = user.uid
            val userDocRef = firestore.collection("user").document(uid)
            userDocRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // 수정불가값
                    tvEmail.text = document.getString("email")
                    tvStudentId.text = document.getString("student_id")

                    // 이름 불러오기
                    document.getString("name")?.let {
                        etName.text = Editable.Factory.getInstance().newEditable(it)
                    }

                    // 학년 불러오기
                    document.getLong("grade")?.let {
                        etGrade.text = Editable.Factory.getInstance().newEditable(it.toString())
                    }

                    // MBTI 불러오기
                    val userMBTI = document.getString("mbti")?:""
                    spinnerMBTI.setSelection(getSpinnerIdx(spinnerDepartments, userMBTI))

                    // 전공 불러오기
                    val userMajor = document.getString("major")?:""
                    spinnerDepartments.setSelection(getSpinnerIdx(spinnerDepartments, userMajor))

                    // 성별 불러오기
                    when (document.getString("sex")) {
                        "남성" -> rdgSex.check(R.id.rdb_NewMale)
                        "여성" -> rdgSex.check(R.id.rdb_NewFemale)
                    }

                    // 상메 불러오기
                    document.getString("profile_message")?.let {
                        etPofileMsg.text = Editable.Factory.getInstance().newEditable(it)
                    }

                    // 생일 값 불러오기
                    val timestamp = document.getTimestamp("birthday")
                    if (timestamp != null) {
                        val date = timestamp.toDate()
                        calendar.time = date
                        selectedYear = calendar.get(Calendar.YEAR)
                        selectedMonth = calendar.get(Calendar.MONTH)
                        selectedDay = calendar.get(Calendar.DAY_OF_MONTH)
                        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
                        tvBirthday?.text = dateFormat.format(date)
                    }
                    else {
                        tvBirthday?.text = "N/A"
                    }

                }
            }.addOnFailureListener { exception ->
                Log.i("프로필 조회 에러", exception.message as String)
            }

            tvBirthday.setOnClickListener {
                // DatePickerDialog 생성 및 표시
                val datePickerDialog = DatePickerDialog(this, { _, year, month, day ->
                    // 선택된 날짜 tv_Birthday
                    val selectedDate = "$year-${month + 1}-$day"
                    tvBirthday.text = selectedDate
                    selectedYear = year
                    selectedMonth = month
                    selectedDay = day
                    // 캘린더 set
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                }, selectedYear, selectedMonth, selectedDay)
                datePickerDialog.show()
            }


            // 뒤로가기
            val close = findViewById<ImageButton>(R.id.ibt_CloseProfileModification)
            close.setOnClickListener {
                finish()
            }

            // 저장
            val setNewData = findViewById<Button>(R.id.bt_SetNewProfile)
            setNewData.setOnClickListener {
                val userMajor = selectedMajor
                val userBirthday: Date = calendar.time
                var userSex: String = ""
                val userName = etName.text.toString().trim()
                if (rdgSex.checkedRadioButtonId != -1) {
                    userSex =
                        findViewById<RadioButton>(rdgSex.checkedRadioButtonId).text.toString() ?: ""
                }
                val userProfileMessage = etPofileMsg.text.toString().trim()
                val grade = etGrade.text.toString().toIntOrNull()?:0
                var userMBTI = ""
                if (selectedMBTI != "#MBTI") {
                    userMBTI = selectedMBTI
                }
                val filter: BadWordFiltering = BadWordFiltering()
                // 학년 검사
                if (grade < 1 || grade > 4) {
                    popUpDialog("잘못된 학년입니다.")
                }
                // 이름 검열
                else if (filter.check(userName)) {
                    popUpDialog("이름에 부적절한 표현이 포함돼있습니다.")
                }
                // 자기소개 검열
                else if (filter.check(userProfileMessage)) {
                    popUpDialog("자기소개에 부적절한 표현이 포함돼있습니다.")
                }
                // 생일 검사
                else if (selectedYear > currentYear - 19) {
                    Log.i("생일", "$selectedYear $currentYear")
                    popUpDialog("잘못된 생일입니다.")
                }
                // 조건 모두 통과
                else {
                    val now = FieldValue.serverTimestamp()
                    val userDB = hashMapOf(
                        "name" to userName,
                        "profile_message" to userProfileMessage,
                        "birthday" to userBirthday,
                        "last_modified" to now,
                        "major" to userMajor,
                        "sex" to userSex,
                        "grade" to grade,
                        "mbti" to userMBTI,
                    )
                    val docRef = firestore.collection("user").document(uid)
                    val resultIntent = Intent()
                    docRef.get().addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            // 이미 있는 애트리뷰트는 업데이트
                            docRef.update(userDB)
                        } else {
                            // 없는 애트리뷰트는 추가
                            docRef.set(userDB)
                        }
                    }.addOnSuccessListener {
                        setResult(Activity.RESULT_OK, resultIntent)
                    }.addOnFailureListener { exception ->
                        popUpDialog("프로필 갱신에 실패했습니다.")
                    }
                    finish()
                }
            }

        }
    }
    fun getSpinnerIdx(sp: Spinner, item: String): Int {
        for (i: Int in 0..sp.count - 1) {
            if (sp.getItemAtPosition(i).toString() == item) {
                return i
            }
        }
        return 0
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