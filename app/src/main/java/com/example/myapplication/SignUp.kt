package com.example.myapplication

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.os.HandlerThread
import android.provider.MediaStore.Audio.Radio
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.sql.Time
import java.util.Date
import java.util.logging.Handler

class SignUp : AppCompatActivity() {
    /*


    추가/수정해야될 사항 :

    회원가입 페이지 "학과" 항목 구현

    이메일 인증버튼 대신 회원가입 버튼만으로 구현
     - 회원가입 버튼 누르면 폼 무결성 검사, 인증메일 전송까지 한번에 실행.
       -> 로직 간소화, 구현 간편화

    회원가입 로직 추가하기
     - db 연동
     - 인증 완료시 회원 정보를 db에 추가함.
     - 추가한 뒤에 "인증 완료" 띄우기
     - 인증 완료 못하면 어차피 1시간 뒤에 authentication 자동삭제


     */
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 학과리스트 드롭다운 내용 연결
        val spinnerDepartments: Spinner = findViewById(R.id.sp_MajorList)

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
        spinnerDepartments.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            // 선택 학과에 저장
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedMajor = parent.getItemAtPosition(position) as String
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedMajor = ""
            }
        }

        // 생일 캘린더 띄우기
        val tvBirthday = findViewById<TextView>(R.id.tv_DatePicker)
        val calendar = Calendar.getInstance()
        // 현재가 기본값
        var currentYear = calendar.get(Calendar.YEAR)
        var selectedYear = calendar.get(Calendar.YEAR)
        var selectedMonth = calendar.get(Calendar.MONTH)
        var selectedDay = calendar.get(Calendar.DAY_OF_MONTH)
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

        // 창 뒤로가기
        val bt_GoBack = findViewById<TextView>(R.id.bt_GoBack)
        bt_GoBack.setOnClickListener {
            finish()
        }



        auth = FirebaseAuth.getInstance()
        val firestoreDB = FirebaseFirestore.getInstance()
        val bt_Submit = findViewById<Button>(R.id.bt_Submit)

        // 인증메일 전송 버튼 클릭시
        bt_Submit.setOnClickListener {
            val inputEmail = findViewById<EditText>(R.id.et_InputEmail)
            val inputPassword = findViewById<EditText>(R.id.et_InputPassword)
            val userEmail: String = inputEmail.text.toString().trim()
            val userPassword: String = inputPassword.text.toString().trim()

            val et_StudentID = findViewById<EditText>(R.id.et_StudentID)
            val inputMajor = spinnerDepartments // sp_MajorList
            val birthday = tvBirthday
            val inputSex = findViewById<RadioGroup>(R.id.rdb_Group)


            val studentID: String = et_StudentID.text.toString().trim()
            val userMajor: String = selectedMajor
            val userBirthday: Date = calendar.time
            val userBirthdayYear = selectedYear
            val userBirthdayMonth = selectedMonth
            val userBirthdayDay = selectedDay
            var userSex: String = ""
            if (inputSex.checkedRadioButtonId != -1) {
                userSex = findViewById<RadioButton>(inputSex.checkedRadioButtonId).text.toString() ?: ""
            }

            // 이메일 검사
            val emailDomain: String = userEmail.split('@').getOrNull(1) ?: "" // 도메인 추출
            // 아이디 비어있나 확인
            if (userEmail.isEmpty()) {
                popUpDialog("이메일을 입력해주세요.")
            }
            // 학교 메일인지 확인
            else if (emailDomain != "syuin.ac.kr") {
                popUpDialog("삼육대학교 이메일(@syuin.ac.kr)을 입력해주세요.")
            }
            // 비밀번호 비어있나 확인
            else if (userPassword.isEmpty()) {
                Log.i("password empty", "비밀번호 공란")
                popUpDialog("비밀번호를 입력해주세요.")
            }
            // 비밀번호 강도 검사
            else if (!isPasswordSecure(userPassword)) {
                popUpDialog("비밀번호는 영어, 숫자, 특수문자를 포함한 8자 이상이어야합니다.")
            }
            // 학번 검사
            else if (studentID.length != 10 && studentID.toInt() > (calendar.get(Calendar.YEAR) + 1) * 1000000) {
                popUpDialog("잘못된 학번입니다.")
            }
            // 전공 선택 안할시
            else if (selectedMajor == "") {
                popUpDialog("학과를 선택해주세요.")
            }
            //날짜 비어있나 확인
            else if (userBirthdayYear > currentYear - 19) {
                popUpDialog("잘못된 생일입니다.")
            }
            //성별 비어있나 검사
            else if (userSex == "") {
                popUpDialog("성별을 입력해주세요.")
            }
            else { // 모든 검사 통과시
                // Firebase authentication에 사용자 등록 요청
                auth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) { // 임시 회원가입 성공
                            // 입력 칸 비활성화
                            inputEmail.isEnabled = false
                            inputPassword.isEnabled = false

                            val user = auth.currentUser
                            var useruid: String = ""
                            if (user != null) {
                                useruid = user.uid
                            }

                            val now = FieldValue.serverTimestamp()
                            val userDB = hashMapOf(
                                "access_level" to 0,
                                "birthday" to userBirthday,
                                "created_at" to now,
                                "email" to userEmail,
                                "is_verified" to 0,
                                "last_modified" to now,
                                "major" to userMajor,
                                "name" to "익명",
                                "sex" to userSex,
                                "student_id" to studentID,
                                "uid" to useruid
                            )
                            firestoreDB.collection("user").document(useruid).set(userDB)

                            //이메일 인증 확인하기 (백그라운드)

                            sendEmailVerification() // 이메일 인증 메일 보내기

                            val builder = AlertDialog.Builder(this)
                            builder.setTitle("")
                            builder.setMessage("인증 메일을 발송했습니다. 인증 후 로그인해주세요.")

                            builder.setPositiveButton("확인") { dialog, _ ->
                                dialog.dismiss()  // 다이얼로그 닫기
                                finish()  // 이전 액티비티로 돌아가기
                            }
                            val dialog: AlertDialog = builder.create()
                            dialog.show()

                        }
                        else { // 회원가입 실패
                            Log.i("signup", "signup failed")
                            val exception = task.exception
                            //중복이메일
                            if (exception is FirebaseAuthUserCollisionException) {
                                Log.w("중복이메일", "이미 등록된 사용자입니다.", exception)
                                popUpDialog("이미 존재하는 이메일입니다.")
                            }
                            //기타 에러
                            else {
                                Toast.makeText(
                                    baseContext, "오류 발생. 나중에 다시 시도해주세요.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
            }

        }
    }

    fun isPasswordSecure(password: String): Boolean {
        // 8자 이상, 영어 숫자 특수문자 각각 1개 이상 포함
        val pattern = Regex("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=])(?=\\S+\$).{8,}\$")
        return pattern.matches(password)
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
    private fun sendEmailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    baseContext, "인증 메일을 전송했습니다. 이메일을 확인하세요.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else {
                Toast.makeText(
                    baseContext, "이메일 인증 메일 전송에 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}