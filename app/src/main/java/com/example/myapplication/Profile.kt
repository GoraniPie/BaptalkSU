package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class Profile : Fragment() {
    private lateinit var changeProfileLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 프로필 수정시 프로필 새로고침
        changeProfileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                // 새로고침
                loadProfile()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 프로필 수정 페이지로 이동
        val btChangeProfile = view.findViewById<ImageButton>(R.id.ibt_ChangeProfile)
        btChangeProfile.setOnClickListener {
            val intent = Intent(context, ProfileChange::class.java)
            changeProfileLauncher.launch(intent)
        }

        // 로그아웃 버튼
        val btLogout = view.findViewById<Button>(R.id.bt_Logout)
        btLogout.setOnClickListener {

        }

        // 프로필 로드
        loadProfile()

    }

    fun loadProfile() {
        // 데이터 표시
        val tvName = view?.findViewById<TextView>(R.id.tv_ProfileName)
        val tvEmail = view?.findViewById<TextView>(R.id.tv_ProfileEmail)
        val tvStudentId = view?.findViewById<TextView>(R.id.tv_ProfileStudentId)
        val tvMajor = view?.findViewById<TextView>(R.id.tv_ProfileMajor)
        val tvBirthday = view?.findViewById<TextView>(R.id.tv_ProfileBirthday)
        val tvSex = view?.findViewById<TextView>(R.id.tv_ProfileSex)
        val tvPofileMsg = view?.findViewById<TextView>(R.id.tv_ProfileStatusMessage)
        val tvGrade = view?.findViewById<TextView>(R.id.tv_ProfileGrade)
        val tvMBTI = view?.findViewById<TextView>(R.id.tv_ProfileMBTI)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val firestore = FirebaseFirestore.getInstance()

        if (user != null) {
            val uid: String = user.uid
            val userDocRef = firestore.collection("user").document(uid)
            userDocRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    tvName?.text = document.getString("name")
                    tvEmail?.text = document.getString("email")
                    tvStudentId?.text = document.getString("student_id")
                    tvMajor?.text = document.getString("major")
                    tvPofileMsg?.text = document.getString("profile_message")
                    tvGrade?.text = document.getLong("grade").toString()
                    tvMBTI?.text = document.getString("mbti")

                    val timestamp = document.getTimestamp("birthday")
                    if (timestamp != null) {
                        val date = timestamp.toDate()
                        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
                        tvBirthday?.text = dateFormat.format(date)
                    }
                    else {
                        tvBirthday?.text = "N/A"
                    }
                    tvSex?.text = document.getString("sex")
                }
                else {
                    tvName?.text = ""
                    tvEmail?.text = ""
                    tvStudentId?.text = ""
                    tvMajor?.text = ""
                    tvBirthday?.text = ""
                    tvSex?.text = ""
                }
            }.addOnFailureListener { exception ->
                Log.i("프로필 조회 에러", exception.message as String)
            }
        }
    }

}