package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
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
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class LogIn : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var stayLoggedIn: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // firebase 초기화
        enableEdgeToEdge()
        setContentView(R.layout.login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 로그인 유지
        stayLoggedIn = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        if (stayLoggedIn.getBoolean("keep_logged_in", false)) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        val signUpIntent: Intent = Intent(this, SignUp::class.java)

        val tv_SignUpForm = findViewById<TextView>(R.id.tv_SignUpForm)
        tv_SignUpForm.setOnClickListener {
            startActivity(signUpIntent)
        }

        val bt_Login = findViewById<Button>(R.id.bt_LogIn)
        bt_Login.setOnClickListener {

            val inputEmail = findViewById<EditText>(R.id.et_InputEmail)
            val inputPassword = findViewById<EditText>(R.id.et_InputPassword)
            val userEmail: String = inputEmail.text.toString().trim()
            val userPassword: String = inputPassword.text.toString().trim()

            if (userEmail.isEmpty()) {
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setTitle("")
                dialogBuilder.setMessage("이메일을 입력해주세요.")
                // 다이얼로그 팝업
                dialogBuilder.setNegativeButton("닫기") { dialog, which ->
                    dialog.dismiss()
                }
                dialogBuilder.create().show()
            }
            else if (userPassword.isEmpty()) {
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setTitle("")
                dialogBuilder.setMessage("비밀번호를 입력해주세요.")
                // 다이얼로그 팝업
                dialogBuilder.setNegativeButton("닫기") { dialog, which ->
                    dialog.dismiss()
                }
                dialogBuilder.create().show()
            }
            else {
                auth = FirebaseAuth.getInstance()
                auth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // 로그인 성공
                            Log.d("LoginActivity", "signInWithEmail:success")
                            val user = auth.currentUser

                            if (user != null) {
                                // 이메일 인증 안된 유저
                                if (!user.isEmailVerified) {
                                    sendEmailVerification()
                                }
                                // 이메일 인증 된 유저
                                else {
                                    val editor = stayLoggedIn.edit()
                                    editor.putBoolean("keep_logged_in", true)
                                    editor.apply()

                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        }
                        else {
                            val dialogBuilder = AlertDialog.Builder(this)
                            dialogBuilder.setTitle("")
                            dialogBuilder.setMessage("로그인에 실패했습니다. ${task.exception?.message}")
                            // 다이얼로그 팝업
                            dialogBuilder.setNegativeButton("닫기") { dialog, which ->
                                dialog.dismiss()
                            }
                            dialogBuilder.create().show()
                        }
                    }
            }
        }
    }
    private fun sendEmailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    baseContext, "인증 메일을 전송했습니다. 인증 완료 후 로그인해주세요.",
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

