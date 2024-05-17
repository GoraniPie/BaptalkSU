package com.example.myapplication

import android.os.Bundle
import android.os.HandlerThread
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import java.sql.Time
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
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 창 뒤로가기
        val bt_GoBack = findViewById<TextView>(R.id.bt_GoBack)
        bt_GoBack.setOnClickListener {
            finish()
        }



        auth = FirebaseAuth.getInstance()

        val bt_SendEmail = findViewById<Button>(R.id.bt_SendVerifyEmail)

        // 인증메일 전송 버튼 클릭시
        bt_SendEmail.setOnClickListener {
            val inputEmail = findViewById<EditText>(R.id.et_InputEmail)
            val inputPassword = findViewById<EditText>(R.id.et_InputPassword)
            val userEmail: String = inputEmail.text.toString().trim()
            val userPassword: String = inputPassword.text.toString().trim()

            // 이메일 검사
            val emailDomain: String = userEmail.split('@').getOrNull(1) ?: "" // 도메인 추출
            // 아이디 비어있나 확인
            if (userEmail.isEmpty()) {
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setTitle("")
                dialogBuilder.setMessage("삼육대학교 이메일(@syuin.ac.kr)을 입력해주세요.")
                // 다이얼로그 팝업
                dialogBuilder.setNegativeButton("닫기") { dialog, which ->
                    dialog.dismiss()
                }
                dialogBuilder.create().show()
            }
            // 학교 메일인지 확인
            else if (emailDomain != "syuin.ac.kr") {
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setTitle("")
                dialogBuilder.setMessage("학교 메일을 입력해주세요.")
                // 다이얼로그 팝업
                dialogBuilder.setNegativeButton("닫기") { dialog, which ->
                    dialog.dismiss()
                }
                dialogBuilder.create().show()
            }
            // 비밀번호 비어있나 확인
            else if (userPassword.isEmpty()) {
                Log.i("password empty", "비밀번호 공란")
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setTitle("")
                dialogBuilder.setMessage("비밀번호를 입력해주세요.")
                // 다이얼로그 팝업
                dialogBuilder.setNegativeButton("닫기") { dialog, which ->
                    dialog.dismiss()
                }
                dialogBuilder.create().show()
            }
            // 비밀번호 강도 검사
            else if (!isPasswordSecure(userPassword)) {
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setTitle("")
                dialogBuilder.setMessage("비밀번호는 영어, 숫자, 특수문자를 포함한 8자 이상이어야합니다.")
                // 다이얼로그 팝업
                dialogBuilder.setNegativeButton("닫기") { dialog, which ->
                    dialog.dismiss()
                }
                dialogBuilder.create().show()
            }
            else { // 모든 검사 통과시
                // Firebase에 사용자 등록 요청
                auth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) { // 임시 회원가입 성공
                            // 입력 칸 비활성화
                            inputEmail.isEnabled = false
                            inputPassword.isEnabled = false

                            val user = auth.currentUser
                            sendEmailVerification() // 이메일 인증 메일 보내기

                            // 10분 안에 인증을 마쳐야함.
                            val startTime = System.currentTimeMillis() // 현재시간
                            val endTime = startTime + 600000 // 인증 제한시간 10분
                            val interval: Long = 3000 // 인증 검사 쿨타임
                            var isTimeOut = true

                            //이메일 인증 확인하기 (백그라운드)
                            val handlerThread = HandlerThread("EmailVerificationThread")
                            handlerThread.start()
                            val handler = android.os.Handler(handlerThread.looper)
                            val checkEmailVerified = object : Runnable {
                                override fun run() {
                                    if (System.currentTimeMillis() < endTime) {
                                        val userTmp = auth.currentUser
                                        // 이메일 인증되었나
                                        if (userTmp != null && userTmp.isEmailVerified) {
                                            isTimeOut = false
                                            runOnUiThread {
                                                Toast.makeText(this@SignUp, "인증 완료. 로그인해주세요.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                        else {
                                            // 3초마다 다시 확인
                                            handler.postDelayed(this, interval)
                                        }
                                    }
                                    else {
                                        if (isTimeOut) {
                                            // 제한시간 끝나면 유저 삭제
                                            val userTmp = auth.currentUser
                                            user?.delete()
                                        }
                                    }
                                }
                            }
                            handler.post(checkEmailVerified)

                        }
                        else { // 회원가입 실패
                            Log.i("signup", "signup failed")
                            val exception = task.exception
                            //중복이메일
                            if (exception is FirebaseAuthUserCollisionException) {
                                Log.w("중복이메일", "이미 등록된 사용자입니다.", exception)
                                val dialogBuilder = AlertDialog.Builder(this)
                                dialogBuilder.setTitle("")
                                dialogBuilder.setMessage("이미 존재하는 이메일입니다.")
                                // 다이얼로그 팝업
                                dialogBuilder.setNegativeButton("닫기") { dialog, which ->
                                    dialog.dismiss()
                                }
                                dialogBuilder.create().show()
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