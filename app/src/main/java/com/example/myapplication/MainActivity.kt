package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recruit)
        val bt_PostNew = findViewById<Button>(R.id.bt_PostNew)
        bt_PostNew.setOnClickListener {
            val intent = Intent(this, PostRecruitment::class.java)
            startActivity(intent)
        }
    }
}