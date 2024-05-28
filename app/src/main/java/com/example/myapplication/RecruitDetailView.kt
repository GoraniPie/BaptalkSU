package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RecruitDetailView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_recruit);

        // 배경 터치시 닫기
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }

        val btClosePopup = findViewById<Button>(R.id.bt_ClosePopup)
        btClosePopup.setOnClickListener {
            Log.i("팝업버튼 닫기", "클릭확인됨")
            finish()
        }
        val btJoin = findViewById<Button>(R.id.bt_EnterRecruit)
        btJoin.setOnClickListener {
            Log.i("참여하기", "클릭됨")
            // join 채팅방
        }
    }
}