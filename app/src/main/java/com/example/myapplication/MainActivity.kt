package com.example.myapplication

import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.RecruitBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding : RecruitBinding
    private lateinit var postRecruitmentLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RecruitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // FCM 파이어베이스 알림설정
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Save token to database
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null && token != null) {
                val database = FirebaseDatabase.getInstance().reference
                database.child("users").child(currentUser.uid).child("fcmToken").setValue(token)
            }
        }

        // 설정버튼 연결
        val btSettings: ImageButton = findViewById<ImageButton>(R.id.ibt_Settings)
        btSettings.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

        // 프래그먼트(모집글조회) 작성 인텐트 끝나면 리사이클뷰 갱신
        postRecruitmentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val fragment = supportFragmentManager.findFragmentById(R.id.frame_layout) as Recruit
                fragment.refreshRecyclerView()
            }
        }

        replaceFragment(Recruit(), "모집")

        binding.bottomNavigationView.setOnItemSelectedListener {
            Log.i("nav bar", "item changed")
            when (it.itemId) {
                R.id.recruit -> replaceFragment(Recruit(), "모집")
                R.id.map -> replaceFragment(Map(), "지도")
                R.id.profile -> replaceFragment(Profile(), "프로필")
                R.id.chat -> replaceFragment(Chat(), "대화방")
            }
            true
        }



    }

    private fun replaceFragment(fragment : Fragment, newText: String){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()

        // 텍스트 뷰의 텍스트 변경
        val textView = findViewById<TextView>(R.id.Title)
        textView.text = newText
    }
    fun launchPostRecruitment() {
        val intent = Intent(this, PostRecruitment::class.java)
        postRecruitmentLauncher.launch(intent)
    }
}