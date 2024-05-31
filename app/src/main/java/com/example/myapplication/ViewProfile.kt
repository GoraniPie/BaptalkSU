package com.example.myapplication

import android.media.Image
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Locale

class ViewProfile : AppCompatActivity() {

    private var firestoreListener: ListenerRegistration? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_profile)

        val goback = findViewById<ImageView>(R.id.iv_GoBack)
        goback.setOnClickListener {
            finish()
        }

        loadProfile()

    }

    fun loadProfile() {
        // 데이터 표시
        Log.i("버튼 로딩", "ㅇㅇ")
        val tvName = findViewById<TextView>(R.id.tv_OtherProfileName)
        val tvMajor = findViewById<TextView>(R.id.tv_OtherProfileMajor)
        val tvBirthday = findViewById<TextView>(R.id.tv_OtherProfileBirthday)
        val tvSex = findViewById<TextView>(R.id.tv_OtherProfileSex)
        val tvPofileMsg = findViewById<TextView>(R.id.tv_OtherProfileStatusMessage)
        val tvGrade = findViewById<TextView>(R.id.tv_OtherProfileGrade)
        val tvMBTI = findViewById<TextView>(R.id.tv_OtherProfileMBTI)


        val firestore = FirebaseFirestore.getInstance()


        val extra = intent.extras
        var uid: String = ""
        if (extra != null) {
            uid = extra.getString("uid").toString()
        }
        val userDocRef = firestore.collection("user").document(uid)
        firestoreListener = userDocRef.addSnapshotListener { document, e ->
            if (e != null) {
                Log.i("프로필 조회 에러", e.message ?: "Unknown error")
                return@addSnapshotListener
            }

            if (document != null && document.exists()) {
                Log.i("프로필 정보 로딩", "ㅇㅇ")
                tvName?.text = document.getString("name")
                tvMajor?.text = document.getString("major")
                tvPofileMsg?.text = document.getString("profile_message")
                tvGrade?.text = document.getLong("grade").toString()
                tvMBTI?.text = document.getString("mbti")

                val timestamp = document.getTimestamp("birthday")
                if (timestamp != null) {
                    val date = timestamp.toDate()
                    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
                    tvBirthday?.text = dateFormat.format(date)
                } else {
                    tvBirthday?.text = "N/A"
                }
                tvSex?.text = document.getString("sex")
            } else {
                tvName?.text = ""
                tvMajor?.text = ""
                tvBirthday?.text = ""
                tvSex?.text = ""
            }
        }

    }
}