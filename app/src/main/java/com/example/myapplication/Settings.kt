package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting)

        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()


        // 계정 생성일
        val tv_createdAt = findViewById<TextView>(R.id.tv_UserCreatedAt)
        val createdAt = firestore.collection("user").document(auth.currentUser?.uid?:"").get()
            .addOnSuccessListener { document->
                val date = document.getTimestamp("created_at")?.toDate()
                val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
                tv_createdAt.text = dateFormat.format(date)
            }

        // 뒤로가기 버튼
        val btBack: ImageButton = findViewById<ImageButton>(R.id.ibt_CloseProfileModification)
        btBack.setOnClickListener {
            finish()
        }

        val btDeleteAcc: TextView = findViewById<TextView>(R.id.tv_DeleteAccount)
        btDeleteAcc.setOnClickListener {
            showDeleteAccountDialog()
        }
    }
    private fun showDeleteAccountDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val editText = EditText(this)
        editText.hint = "탈퇴하겠습니다."

        dialogBuilder.setTitle("탈퇴하시겠습니까?")
        dialogBuilder.setMessage("탈퇴하면 계정의 모든 작성글과 회원정보가 삭제되며, 되돌릴 수 없게 됩니다.\n탈퇴하려면 \"탈퇴하겠습니다.\"를 입력하세요.")
        dialogBuilder.setView(editText)

        dialogBuilder.setPositiveButton("탈퇴하기") { dialogInterface, _ ->
            val inputText = editText.text.toString().trim()
            if (inputText == "탈퇴하겠습니다.") {
                reauthenticateAndDeleteAccount()
            } else {
                showErrorDialog()
            }
            dialogInterface.dismiss()
        }
        dialogBuilder.setNegativeButton("닫기") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.setOnShowListener {
            editText.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            Handler(Looper.getMainLooper()).postDelayed({
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }, 100)
        }
        dialog.setOnDismissListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        }
        dialog.show()
    }

    private fun reauthenticateAndDeleteAccount() {
        val auth = FirebaseAuth.getInstance()
        val user: FirebaseUser? = auth.currentUser
        if (user != null) {
            val userEmail = user.email
            if (userEmail != null) {
                val passwordDialog = AlertDialog.Builder(this)
                val passwordEditText = EditText(this)
                passwordEditText.hint = "비밀번호를 입력하세요"

                passwordDialog.setTitle("재인증 필요")
                passwordDialog.setMessage("계속하려면 비밀번호를 입력하세요.")
                passwordDialog.setView(passwordEditText)

                passwordDialog.setPositiveButton("확인") { _, _ ->
                    val password = passwordEditText.text.toString().trim()
                    if (password.isNotEmpty()) {
                        val credential = EmailAuthProvider.getCredential(userEmail, password)
                        user.reauthenticate(credential).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                deleteUserAccount(user)
                            } else {
                                Toast.makeText(this, "재인증 실패: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "비밀번호를 입력하세요", Toast.LENGTH_LONG).show()
                    }
                }
                passwordDialog.setNegativeButton("취소") { dialog, _ ->
                    dialog.dismiss()
                }

                passwordDialog.create().show()
            }
        }
    }

    private fun deleteUserAccount(user: FirebaseUser) {
        val uid = user.uid
        user.delete().addOnSuccessListener {
            // cloud firestore 유저 정보 삭제
            val firestoreDB = FirebaseFirestore.getInstance()
            firestoreDB.collection("user").document(uid).delete().addOnSuccessListener {
                FirebaseAuth.getInstance().signOut()
                showCompletionDialog()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "유저 정보 삭제 실패: ${e.message}", Toast.LENGTH_LONG).show()
            }
            firestoreDB.collection("recruitment").whereEqualTo("uploader_id", uid)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // 각 문서 삭제
                        firestoreDB.collection("recruitment").document(document.id)
                            .delete()
                            .addOnFailureListener { e ->
                                Log.w("모든 작성글 삭제", "실패함", e)
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("DeleteDocuments", "Error getting documents: ", exception)
                }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "계정 삭제 실패: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showCompletionDialog() {
        // 로그인 유지 인증 정보 삭제
        val dialogBuilder = AlertDialog.Builder(this)
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        FirebaseAuth.getInstance().signOut()

        dialogBuilder.setTitle("탈퇴 완료")
        dialogBuilder.setMessage("메인 화면으로 돌아갑니다.")
        dialogBuilder.setNegativeButton("닫기") { dialog, _ ->
            // 다이얼로그 닫힘 후 약간의 지연을 두고 액티비티 전환
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
                try {
                    // 캐시 삭제
                    val cacheDir = this.cacheDir
                    deleteDir(cacheDir)

                    // 파일 디렉토리 삭제
                    val filesDir = this.filesDir
                    deleteDir(filesDir)

                    // SharedPreferences 삭제
                    val sharedPreferences = this.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()

                    // 다른 SharedPreferences 파일들도 삭제
                    val prefsDir = File(this.filesDir.parent, "shared_prefs")
                    deleteDir(prefsDir)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val intent = Intent(this, Splash::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                Runtime.getRuntime().exit(0)
            }, 300)
        }
        dialogBuilder.create().show()
    }

    private fun showErrorDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("오류")
        dialogBuilder.setMessage("확인 문장이 일치하지 않습니다.")
        dialogBuilder.setNegativeButton("닫기") { dialog, _ ->
            dialog.dismiss()
        }
        dialogBuilder.create().show()
    }
    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (child in children) {
                    val success = deleteDir(File(dir, child))
                    if (!success) {
                        return false
                    }
                }
            }
        }
        // 디렉토리가 비었거나 파일이면 삭제
        return dir?.delete() ?: false
    }
}