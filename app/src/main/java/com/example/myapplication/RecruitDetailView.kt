package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
interface ParentActivityListener {
    fun onCloseParentActivity()
}
class RecruitDetailView : AppCompatActivity(), ParentActivityListener {
    private lateinit var modifyRecruitLauncher: ActivityResultLauncher<Intent>

    override fun onCloseParentActivity() {
        setResult(Activity.RESULT_OK)
        finish()
    }
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

        val btClosePopup = findViewById<ImageButton>(R.id.ibt_ClosePopup)
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