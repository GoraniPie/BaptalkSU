package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        lifecycleScope.launch {
            delay(1200) // 로딩화면 1.2초
            val intent = Intent(this@Splash, LogIn::class.java)
            startActivity(intent)
            finish()
        }
    }
}